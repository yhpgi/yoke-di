package io.github.yhpgi.yoke.processor

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * The core annotation processor for Yoke DI.
 *
 * This processor scans the classpath for Yoke annotations (`@YokeEntryPoint`, `@YokeComponent`,
 * `@ContributesTo`, `@Injectable`, etc.), builds a complete dependency graph, and then
 * generates the necessary implementation code for the DI containers and resolution logic.
 *
 * @property codeGenerator The KSP code generator instance.
 * @property logger The KSP logger for reporting errors and warnings.
 */
class YokeProcessor(
  private val codeGenerator: CodeGenerator, private val logger: KSPLogger
) : SymbolProcessor {

  private val providerCn = ClassName("io.github.yhpgi.yoke.di", "Provider")
  private val lazyCn = ClassName("io.github.yhpgi.yoke.di", "LazyProvider")
  private val singletonCn = ClassName("io.github.yhpgi.yoke.di", "SingletonProvider")
  private val containerCn = ClassName("io.github.yhpgi.yoke.di", "DIContainer")
  private val viewModelCn = ClassName("androidx.lifecycle", "ViewModel")
  private val savedStateHandleCn = ClassName("androidx.lifecycle", "SavedStateHandle")
  private val createSavedStateHandleMn = MemberName("androidx.lifecycle", "createSavedStateHandle")
  private val viewModelProviderCn = ClassName("io.github.yhpgi.yoke.di", "ViewModelProvider")
  private val generatedFiles = mutableSetOf<ClassName>()

  /**
   * The main processing entry point for KSP.
   * This method is invoked by the KSP framework on each processing round.
   *
   * @param resolver The KSP resolver, providing access to symbols and types.
   * @return A list of symbols that could not be processed in the current round.
   */
  override fun process(resolver: Resolver): List<KSAnnotated> {
    val entryPoint = resolver.getSymbolsWithAnnotation("io.github.yhpgi.yoke.annotation.YokeEntryPoint")
      .filterIsInstance<KSClassDeclaration>().firstOrNull()

    if (entryPoint == null) {
      if (resolver.getAllFiles().any { it.declarations.any() }) {
        logger.info("No @YokeEntryPoint found, skipping processing.")
      }
      return emptyList()
    }

    val allComponents =
      (resolver.getSymbolsWithAnnotation("io.github.yhpgi.yoke.annotation.YokeComponent") + resolver.getSymbolsWithAnnotation(
        "io.github.yhpgi.yoke.annotation.YokeSubcomponent"
      )).filterIsInstance<KSClassDeclaration>().toList()

    val contributionMap = buildContributionMap(resolver)
    val parentMap = buildParentComponentMap(contributionMap)
    val graph = buildDependencyGraph(contributionMap)
    detectCycles(graph)
    val allFiles = resolver.getAllFiles().toList().toTypedArray()

    allComponents.forEach { component ->
      generateComponent(component, graph, parentMap, allFiles)
    }

    generateEntryPoint(entryPoint, graph, parentMap, allFiles)

    return emptyList()
  }

  /**
   * Builds a map from each component (`KSClassDeclaration`) to the list of declarations
   * that contribute to it via the `@ContributesTo` annotation.
   */
  private fun buildContributionMap(resolver: Resolver): Map<KSClassDeclaration, List<KSDeclaration>> {
    val map = mutableMapOf<KSClassDeclaration, MutableList<KSDeclaration>>()

    resolver.getSymbolsWithAnnotation("io.github.yhpgi.yoke.annotation.ContributesTo").filterIsInstance<KSDeclaration>()
      .filter { it.validate() }.forEach { contribution ->
        contribution.annotations.filter { it.shortName.asString() == "ContributesTo" }.forEach { annotation ->
          val scopeType = annotation.arguments.find { it.name?.asString() == "scope" }?.value as? KSType
          val targetComponent = scopeType?.declaration as? KSClassDeclaration
          if (targetComponent != null) {
            map.getOrPut(targetComponent) { mutableListOf() }.add(contribution)
          } else {
            logger.error(
              "Invalid @ContributesTo annotation: Unable to resolve target component. " + "Ensure the scope parameter is a valid @YokeComponent or @YokeSubcomponent class.",
              contribution
            )
          }
        }
      }

    return map
  }

  /**
   * Builds a map from each component to its parent component.
   * Subcomponents are mapped to their parent, while root components are mapped to null.
   */
  private fun buildParentComponentMap(contributionMap: Map<KSClassDeclaration, List<KSDeclaration>>): Map<KSClassDeclaration, KSClassDeclaration?> {
    val map = mutableMapOf<KSClassDeclaration, KSClassDeclaration?>()

    contributionMap.forEach { (parent, contributions) ->
      map.putIfAbsent(parent, null)
      contributions.filterIsInstance<KSClassDeclaration>().filter { it.isSubcomponent() }
        .forEach { sub -> map[sub] = parent }
    }

    return map
  }

  /**
   * Finds the component that "hosts" a given declaration (class or function).
   * A host component is the component specified in the `@ContributesTo` annotation
   * of the declaration or its enclosing module.
   */
  private fun findHostComponent(
    decl: KSDeclaration, contributionMap: Map<KSClassDeclaration, List<KSDeclaration>>
  ): KSClassDeclaration? {
    val parentDecl = if (decl is KSFunctionDeclaration) decl.parentDeclaration else decl
    return contributionMap.entries.firstOrNull { (_, contributions) -> parentDecl in contributions }?.key
  }

  /**
   * Builds the complete dependency graph for the application.
   * The graph is a map where keys are `DependencyKey`s (type + qualifier) and values
   * are `DependencyNode`s, which contain all metadata about how to provide the dependency.
   */
  private fun buildDependencyGraph(contributionMap: Map<KSClassDeclaration, List<KSDeclaration>>): Map<DependencyKey, DependencyNode> {
    val graph = mutableMapOf<DependencyKey, DependencyNode>()

    contributionMap.forEach { (component, contributions) ->
      val componentScope = component.getScope()

      contributions.forEach { contribution ->
        val hostComponent = findHostComponent(contribution, contributionMap)
        if (hostComponent == null) {
          logger.error(
            "Unable to determine host component for ${contribution.qualifiedName?.asString()}. " + "Ensure this declaration has a valid @ContributesTo annotation pointing to a @YokeComponent or @YokeSubcomponent.",
            contribution
          )
          return@forEach
        }

        when (contribution) {
          is KSClassDeclaration if (contribution.isInjectable() || contribution.getScope() != null) && contribution.classKind != ClassKind.INTERFACE -> {
            parseInjectableClass(contribution, componentScope, hostComponent)?.let { node ->
              graph[node.key] = node

              node.bindings.forEach { bindingKey ->
                graph[bindingKey] = node.copy(key = bindingKey, isBinding = true)
              }

              if (node.assistedParams.isNotEmpty()) {
                val factoryInterface = contribution.getAssistedFactoryInterface()
                if (factoryInterface != null) {
                  val factoryKey = DependencyKey(factoryInterface.toTypeName(), null)
                  graph[factoryKey] = node.copy(
                    key = factoryKey, assistedParams = emptyList(), isFactory = true
                  )
                } else {
                  logger.error(
                    "Class ${contribution.simpleName.asString()} has @Assisted parameters but no Factory interface. " + "Define a nested interface named 'Factory' with a 'create' method that accepts the @Assisted parameters.",
                    contribution
                  )
                }
              }
            }
          }

          is KSClassDeclaration if contribution.isModule() -> {
            contribution.getAllFunctions().forEach { func ->
              when {
                func.isProvides() -> parseProvidesFunction(
                  func, contribution, componentScope, hostComponent
                )?.let { graph[it.key] = it }

                func.isBinds() -> parseBindsFunction(func, componentScope, hostComponent)?.let { graph[it.key] = it }
              }
            }
          }
        }
      }
    }

    return graph
  }

  /**
   * Parses an `@Injectable` class and converts it into a `DependencyNode`.
   */
  private fun parseInjectableClass(
    symbol: KSClassDeclaration, componentScope: ClassName?, hostComponent: KSClassDeclaration
  ): DependencyNode? {
    val constructor = symbol.primaryConstructor
    if (constructor == null) {
      logger.error(
        "Class ${symbol.simpleName.asString()} is marked @Injectable but has no primary constructor. " + "Add a primary constructor to enable dependency injection.",
        symbol
      )
      return null
    }

    val key = DependencyKey(symbol.asStarProjectedType().toTypeName(), symbol.getQualifier())
    val scope = symbol.getScope() ?: componentScope

    val bindsAnnotation = symbol.annotations.find { it.shortName.asString() == "Binds" }
    val explicitBind =
      (bindsAnnotation?.arguments?.find { it.name?.asString() == "to" }?.value as? KSType)?.takeUnless { it.toClassName() == Nothing::class.asClassName() }
        ?.let { bindType ->
          if (!symbol.getAllSuperTypes().any { it.toTypeName() == bindType.toTypeName() }) {
            logger.error(
              "Invalid @Binds annotation: ${symbol.simpleName.asString()} does not implement or extend ${bindType.toTypeName()}. " + "Ensure the 'to' parameter specifies a valid supertype.",
              symbol
            )
            null
          } else {
            DependencyKey(bindType.toTypeName(), symbol.getQualifier())
          }
        }

    val regularParams = constructor.parameters.filter { !it.isAssisted() && !it.isSavedStateHandle() }
    val assistedParams = constructor.parameters.filter { it.isAssisted() }
    val hasSavedStateHandle = constructor.parameters.any { it.isSavedStateHandle() }

    return DependencyNode(
      key = key,
      declaration = symbol,
      dependencies = regularParams.map { it.toDependencyKey(symbol, logger) },
      assistedParams = assistedParams.map { Param(it.name!!, it.toDependencyKey(symbol, logger)) },
      bindings = explicitBind?.let { listOf(it) } ?: emptyList(),
      scope = scope,
      hostComponent = hostComponent,
      isBinding = false,
      isFactory = false,
      isViewModel = symbol.isViewModel(),
      needsSavedStateHandle = hasSavedStateHandle)
  }

  /**
   * Parses a `@Provides` function from a `@Module` and converts it into a `DependencyNode`.
   */
  private fun parseProvidesFunction(
    func: KSFunctionDeclaration,
    module: KSClassDeclaration,
    componentScope: ClassName?,
    hostComponent: KSClassDeclaration
  ): DependencyNode? {
    val returnType = func.returnType?.resolve()
    if (returnType == null || returnType.isError) {
      logger.error(
        "Unable to resolve return type for @Provides function ${func.simpleName.asString()} in module ${module.simpleName.asString()}. " + "Ensure the return type is valid and all dependencies are available.",
        func
      )
      return null
    }

    val key = DependencyKey(returnType.toTypeName(), func.getQualifier())
    val scope = func.getScope() ?: componentScope

    return DependencyNode(
      key = key,
      declaration = func,
      dependencies = func.parameters.map { it.toDependencyKey(func, logger) },
      assistedParams = emptyList(),
      bindings = emptyList(),
      scope = scope,
      hostComponent = hostComponent,
      isBinding = false,
      isFactory = false,
      providesModule = module,
      isViewModel = (returnType.declaration as? KSClassDeclaration)?.isViewModel() == true
    )
  }

  /**
   * Parses a `@Binds` function from a `@Module` and converts it into a `DependencyNode`.
   */
  private fun parseBindsFunction(
    func: KSFunctionDeclaration, componentScope: ClassName?, hostComponent: KSClassDeclaration
  ): DependencyNode? {
    if (!func.modifiers.contains(Modifier.ABSTRACT)) {
      logger.error(
        "@Binds function ${func.simpleName.asString()} must be abstract. " + "Change the function to: abstract fun ${func.simpleName.asString()}(...): ReturnType",
        func
      )
      return null
    }

    val implParam = func.parameters.singleOrNull()
    if (implParam == null) {
      logger.error(
        "@Binds function ${func.simpleName.asString()} must have exactly one parameter (the implementation). " + "Example: @Binds abstract fun bindRepository(impl: RepositoryImpl): Repository",
        func
      )
      return null
    }

    val returnType = func.returnType?.resolve()
    if (returnType == null || returnType.isError) {
      logger.error(
        "Unable to resolve return type for @Binds function ${func.simpleName.asString()}. " + "Ensure the return type is a valid interface or superclass.",
        func
      )
      return null
    }

    val key = DependencyKey(returnType.toTypeName(), func.getQualifier())
    val implKey = implParam.toDependencyKey(func, logger)
    val scope = func.getScope() ?: componentScope

    return DependencyNode(
      key = key,
      declaration = func,
      dependencies = listOf(implKey),
      assistedParams = emptyList(),
      bindings = emptyList(),
      scope = scope,
      hostComponent = hostComponent,
      isBinding = true,
      isFactory = false,
      isViewModel = (returnType.declaration as? KSClassDeclaration)?.isViewModel() == true
    )
  }

  /**
   * Generates the implementation class for a `@YokeComponent` or `@YokeSubcomponent`.
   * This class will contain properties for each provider belonging to the component.
   */
  private fun generateComponent(
    component: KSClassDeclaration,
    graph: Map<DependencyKey, DependencyNode>,
    parentMap: Map<KSClassDeclaration, KSClassDeclaration?>,
    sources: Array<KSFile>
  ) {
    val componentCn = component.toClassName()
    val implCn = ClassName(componentCn.packageName, "${componentCn.simpleName}Impl")

    if (implCn in generatedFiles) return
    generatedFiles.add(implCn)

    val componentScope = component.getScope()
    val localNodes = graph.values.filter { it.hostComponent.toClassName() == componentCn }.distinctBy { it.key }
    val localKeys = localNodes.map { it.key }.toSet()
    val parentDecl = parentMap[component]
    val bindsInstanceParams = component.primaryConstructor?.parameters?.filter { it.isBindsInstance() } ?: emptyList()

    val fileSpec = FileSpec.builder(implCn).apply {
      addType(
        TypeSpec.classBuilder(implCn).addModifiers(KModifier.INTERNAL).addSuperinterface(componentCn)
          .addSuperinterface(containerCn).apply {
            val constructorBuilder = FunSpec.constructorBuilder()

            if (parentDecl != null) {
              val parentImplCn = ClassName(parentDecl.packageName.asString(), "${parentDecl.simpleName.asString()}Impl")
              constructorBuilder.addParameter("parent", parentImplCn)
              addProperty(PropertySpec.builder("parent", parentImplCn, KModifier.PRIVATE).initializer("parent").build())
            }

            bindsInstanceParams.forEach { param ->
              val paramType = param.type.resolve().toTypeName()
              val paramName = param.name!!.asString()
              constructorBuilder.addParameter(paramName, paramType)
              addProperty(
                PropertySpec.builder(paramName, paramType, KModifier.PRIVATE).initializer(paramName).build()
              )

              val bindsKey = DependencyKey(paramType, param.getQualifier())
              addProperty(
                PropertySpec.builder(
                  bindsKey.toProviderName(), providerCn.parameterizedBy(paramType), KModifier.INTERNAL
                ).initializer(CodeBlock.of("%T { %L }", lazyCn, paramName)).build()
              )
            }

            primaryConstructor(constructorBuilder.build())

            localNodes.forEach { node ->
              addProperty(generateProviderProperty(node, localKeys, componentScope, parentDecl))
            }

            parentMap.entries.filter { it.value == component }.forEach { (subcomponent, _) ->
              val subImplCn =
                ClassName(subcomponent.packageName.asString(), "${subcomponent.simpleName.asString()}Impl")
              val factoryMethod = subcomponent.simpleName.asString().replaceFirstChar(Char::lowercase)

              addFunction(
                FunSpec.builder(factoryMethod).addModifiers(KModifier.INTERNAL).returns(subImplCn)
                  .addStatement("return %T(this)", subImplCn).build()
              )
            }
          }.build()
      )
    }.build()

    fileSpec.writeTo(codeGenerator, Dependencies(true, *sources))
  }

  /**
   * Generates a `Provider<T>` property for a given `DependencyNode` inside a component implementation.
   */
  private fun generateProviderProperty(
    node: DependencyNode, localKeys: Set<DependencyKey>, componentScope: ClassName?, parentDecl: KSClassDeclaration?
  ): PropertySpec {
    val providerName = node.key.toProviderName()
    val providerType = if (node.isViewModel) {
      viewModelProviderCn.parameterizedBy(node.key.typeName)
    } else {
      providerCn.parameterizedBy(node.key.typeName)
    }

    return when {
      node.isBinding && node.dependencies.isNotEmpty() -> {
        val sourceDep = node.dependencies.first()
        val sourceProvider = if (sourceDep in localKeys) {
          sourceDep.toProviderName()
        } else if (parentDecl != null) {
          "parent.${sourceDep.toProviderName()}"
        } else {
          sourceDep.toProviderName()
        }

        PropertySpec.builder(providerName, providerType, KModifier.INTERNAL).getter(
          FunSpec.getterBuilder().addStatement("return %L", sourceProvider).build()
        ).build()
      }

      node.isFactory -> {
        val concreteNode = (node.declaration as KSClassDeclaration)
        val factoryImpl = TypeSpec.anonymousClassBuilder().addSuperinterface(node.key.typeName).addFunction(
          FunSpec.builder("create").addModifiers(KModifier.OVERRIDE).returns(concreteNode.toClassName()).apply {
            concreteNode.primaryConstructor!!.parameters.forEach { param ->
              if (param.isAssisted()) {
                addParameter(param.name!!.asString(), param.type.resolve().toTypeName())
              }
            }

            val args = concreteNode.primaryConstructor!!.parameters.joinToString(", ") { param ->
              if (param.isAssisted()) {
                param.name!!.asString()
              } else {
                val depKey = param.toDependencyKey(node.declaration, logger)
                val depProvider = if (depKey in localKeys) {
                  depKey.toProviderName()
                } else if (parentDecl != null) {
                  "parent.${depKey.toProviderName()}"
                } else {
                  depKey.toProviderName()
                }
                "$depProvider.get()"
              }
            }

            addStatement("return %T($args)", concreteNode.toClassName())
          }.build()
        ).build()

        PropertySpec.builder(providerName, providerType, KModifier.INTERNAL)
          .initializer(CodeBlock.of("%T { %L }", lazyCn, factoryImpl)).build()
      }

      else -> {
        val initializer = buildProviderInitializer(node, localKeys, componentScope, parentDecl)

        PropertySpec.builder(providerName, providerType, KModifier.INTERNAL).initializer(initializer).build()
      }
    }
  }

  /**
   * Builds the `CodeBlock` used to initialize a provider property.
   * It handles scoping (`SingletonProvider`) and regular instantiation (`LazyProvider`).
   */
  private fun buildProviderInitializer(
    node: DependencyNode, localKeys: Set<DependencyKey>, componentScope: ClassName?, parentDecl: KSClassDeclaration?
  ): CodeBlock {
    val baseProvider = when {
      node.assistedParams.isNotEmpty() -> {
        CodeBlock.of(
          "%T { error(%S) }",
          lazyCn,
          "Cannot inject `${node.key.typeName}` directly. Inject its Factory interface instead."
        )
      }

      node.providesModule != null -> {
        val args = node.dependencies.joinToString(", ") { depKey ->
          val depProvider = if (depKey in localKeys) {
            depKey.toProviderName()
          } else if (parentDecl != null) {
            "parent.${depKey.toProviderName()}"
          } else {
            depKey.toProviderName()
          }
          "$depProvider.get()"
        }
        CodeBlock.of(
          "%T { %T.%L($args) }",
          lazyCn,
          node.providesModule.toClassName(),
          (node.declaration as KSFunctionDeclaration).simpleName.asString()
        )
      }

      node.isViewModel && node.needsSavedStateHandle -> {
        val constructor = (node.declaration as KSClassDeclaration).primaryConstructor!!
        constructor.parameters.filter { !it.isSavedStateHandle() }

        CodeBlock.builder().apply {
          add("%T { extras ->\n", viewModelProviderCn)
          indent()

          val constructorArgs = constructor.parameters.map { param ->
            when {
              param.isSavedStateHandle() -> CodeBlock.of("extras.%M()", createSavedStateHandleMn)
              else -> {
                val depKey = param.toDependencyKey(node.declaration, logger)
                val depProvider = if (depKey in localKeys) {
                  depKey.toProviderName()
                } else if (parentDecl != null) {
                  "parent.${depKey.toProviderName()}"
                } else {
                  depKey.toProviderName()
                }
                CodeBlock.of("%L.get()", depProvider)
              }
            }
          }

          add("%T(\n", node.declaration.toClassName())
          indent()
          add(constructorArgs.joinToCode(",\n"))
          unindent()
          add("\n)")
          unindent()
          add("\n}")
        }.build()
      }

      node.isViewModel -> {
        val args = node.dependencies.joinToString(", ") { depKey ->
          val depProvider = if (depKey in localKeys) {
            depKey.toProviderName()
          } else if (parentDecl != null) {
            "parent.${depKey.toProviderName()}"
          } else {
            depKey.toProviderName()
          }
          "$depProvider.get()"
        }
        CodeBlock.of(
          "%T { _ -> %T($args) }", viewModelProviderCn, (node.declaration as KSClassDeclaration).toClassName()
        )
      }

      else -> {
        val args = node.dependencies.joinToString(", ") { depKey ->
          val depProvider = if (depKey in localKeys) {
            depKey.toProviderName()
          } else if (parentDecl != null) {
            "parent.${depKey.toProviderName()}"
          } else {
            depKey.toProviderName()
          }
          "$depProvider.get()"
        }
        CodeBlock.of("%T { %T($args) }", lazyCn, (node.declaration as KSClassDeclaration).toClassName())
      }
    }

    return if (node.scope != null && componentScope == node.scope && !node.isViewModel) {
      CodeBlock.of("%T(%L)", singletonCn, baseProvider)
    } else {
      baseProvider
    }
  }

  /**
   * Generates the main `YokeGenerated.kt` file, which contains the public entry points
   * for the DI framework, such as `YokeApplication` and the `GeneratedYokeResolver`.
   */
  private fun generateEntryPoint(
    entryPoint: KSClassDeclaration,
    graph: Map<DependencyKey, DependencyNode>,
    parentMap: Map<KSClassDeclaration, KSClassDeclaration?>,
    sources: Array<KSFile>
  ) {
    val packageName = "io.github.yhpgi.yoke.di"
    val rootComponent = parentMap.entries.find { it.value == null }?.key

    if (rootComponent == null) {
      logger.error(
        "No root @YokeComponent found. Ensure you have a component annotated with @YokeComponent that is not a subcomponent.",
        entryPoint
      )
      return
    }
    val rootImplCn = ClassName(rootComponent.packageName.asString(), "${rootComponent.simpleName.asString()}Impl")
    val bindsInstanceParams =
      rootComponent.primaryConstructor?.parameters?.filter { it.isBindsInstance() } ?: emptyList()

    val fileSpec = FileSpec.builder(packageName, "YokeGenerated").apply {
      addType(generateResolver(graph, parentMap, rootComponent))
      addFunction(generateYokeApplication(rootImplCn, bindsInstanceParams))
      addFunction(generateYokeInitializer(rootImplCn, bindsInstanceParams))
    }.build()

    fileSpec.writeTo(codeGenerator, Dependencies(true, *sources))
  }

  /**
   * Generates the `GeneratedYokeResolver` object, which implements the `YokeResolver`
   * interface and contains the logic for resolving any dependency in the graph.
   */
  private fun generateResolver(
    graph: Map<DependencyKey, DependencyNode>,
    parentMap: Map<KSClassDeclaration, KSClassDeclaration?>,
    rootComponent: KSClassDeclaration
  ): TypeSpec {
    val yokeContextCn = ClassName("io.github.yhpgi.yoke.di", "YokeContext")

    return TypeSpec.objectBuilder("GeneratedYokeResolver").addModifiers(KModifier.INTERNAL)
      .addSuperinterface(ClassName("io.github.yhpgi.yoke.di", "YokeResolver")).apply {
        addFunction(
          FunSpec.builder("getProvider").addModifiers(KModifier.OVERRIDE)
            .addAnnotation(ClassName("androidx.compose.runtime", "Composable"))
            .addTypeVariable(TypeVariableName("T", ANY))
            .addParameter("kClass", ClassName("kotlin.reflect", "KClass").parameterizedBy(TypeVariableName("T")))
            .addParameter(
              "qualifier", ClassName("kotlin.reflect", "KClass").parameterizedBy(STAR).copy(nullable = true)
            ).returns(ClassName("io.github.yhpgi.yoke.di", "Provider").parameterizedBy(TypeVariableName("T")))
            .addStatement("return getProvider(%T.current, kClass, qualifier)", yokeContextCn).build()
        )

        addFunction(
          FunSpec.builder("getProvider").addModifiers(KModifier.OVERRIDE).addTypeVariable(TypeVariableName("T", ANY))
            .addParameter("context", yokeContextCn)
            .addParameter("kClass", ClassName("kotlin.reflect", "KClass").parameterizedBy(TypeVariableName("T")))
            .addParameter(
              "qualifier", ClassName("kotlin.reflect", "KClass").parameterizedBy(STAR).copy(nullable = true)
            ).returns(ClassName("io.github.yhpgi.yoke.di", "Provider").parameterizedBy(TypeVariableName("T")))
            .addCode(buildResolverCode(graph, parentMap, rootComponent)).build()
        )
      }.build()
  }

  /**
   * Builds the `CodeBlock` for the `getProvider` method in the resolver.
   * This contains a large `when` statement that maps a `(KClass, KClass?)` pair
   * to the correct provider property on the correct component implementation.
   */
  private fun buildResolverCode(
    graph: Map<DependencyKey, DependencyNode>,
    parentMap: Map<KSClassDeclaration, KSClassDeclaration?>,
    rootComponent: KSClassDeclaration
  ): CodeBlock {
    return CodeBlock.builder().apply {
      addStatement("val yokeContext = context")
      beginControlFlow(
        "val provider: %T<%T> = when (kClass to qualifier)", ClassName("io.github.yhpgi.yoke.di", "Provider"), ANY
      )

      parentMap.keys.filter { it.isSubcomponent() }.forEach { sub ->
        val parent = parentMap.getValue(sub)
        val subImplCn = ClassName(sub.packageName.asString(), "${sub.simpleName.asString()}Impl")
        val parentImplCn = ClassName(parent!!.packageName.asString(), "${parent.simpleName.asString()}Impl")
        val factoryMethod = sub.simpleName.asString().replaceFirstChar(Char::lowercase)

        addStatement(
          "%T::class to null -> %T { yokeContext.getOrCreate(%T::class) { root -> (root as %T).%L() } as %T }",
          sub.toClassName(),
          ClassName("io.github.yhpgi.yoke.di", "LazyProvider"),
          subImplCn,
          parentImplCn,
          factoryMethod,
          ANY
        )
      }

      graph.entries.sortedBy { it.key.typeName.toString() }.forEach { (key, node) ->
        val hostComponent = node.hostComponent
        val hostImplCn = ClassName(hostComponent.packageName.asString(), "${hostComponent.simpleName.asString()}Impl")

        val qualifierPattern = if (key.qualifier != null) {
          "%T::class to %T::class"
        } else {
          "%T::class to null"
        }

        val qualifierArgs = if (key.qualifier != null) {
          listOf(key.typeName, key.qualifier)
        } else {
          listOf(key.typeName)
        }

        if (hostComponent == rootComponent) {
          addStatement(
            "$qualifierPattern -> (yokeContext.root as %T).%L",
            *qualifierArgs.toTypedArray(),
            hostImplCn,
            key.toProviderName()
          )
        } else {
          val parent = parentMap.getValue(hostComponent)
          val parentImplCn = ClassName(parent!!.packageName.asString(), "${parent.simpleName.asString()}Impl")
          val factoryMethod = hostComponent.simpleName.asString().replaceFirstChar(Char::lowercase)

          addStatement(
            "$qualifierPattern -> yokeContext.getOrCreate(%T::class) { root -> (root as %T).%L() }.%L",
            *qualifierArgs.toTypedArray(),
            hostImplCn,
            parentImplCn,
            factoryMethod,
            key.toProviderName()
          )
        }
      }

      addStatement(
        "else -> error(%P)",
        "Yoke DI Error: No provider found for \${kClass.simpleName}\${qualifier?.let { \" qualified by \${it.simpleName}\" } ?: \"\"}.\n\n" + "Possible solutions:\n" + "1. Ensure the class is annotated with @Injectable and @ContributesTo(YourComponent::class)\n" + "2. For interfaces, check that an implementation is bound using @Binds\n" + "3. For external dependencies, provide them via @Provides in a @Module\n" + "4. If using qualifiers, verify both the provider and injection site use matching qualifiers\n" + "5. Ensure the dependency is available in the correct component scope"
      )

      endControlFlow()
      addStatement("@Suppress(\"UNCHECKED_CAST\")")
      addStatement(
        "return provider as %T<%T>", ClassName("io.github.yhpgi.yoke.di", "Provider"), TypeVariableName("T")
      )
    }.build()
  }

  /**
   * Generates a helper function to create the root component instance.
   * This is used for non-composable initialization.
   */
  private fun generateYokeInitializer(rootImplCn: ClassName, bindsInstanceParams: List<KSValueParameter>): FunSpec {
    return FunSpec.builder("createYokeRoot").addModifiers(KModifier.INTERNAL).returns(containerCn).apply {
      bindsInstanceParams.forEach { param ->
        addParameter(param.name!!.asString(), param.type.resolve().toTypeName())
      }
      val args = bindsInstanceParams.joinToString(", ") { it.name!!.asString() }
      addStatement("return %T($args)", rootImplCn)
    }.build()
  }

  /**
   * Generates the `YokeApplication` Composable function. This function acts as the root
   * of a Yoke-powered application, setting up the root DI container, `YokeContext`,
   * and providing them to the rest of the app via `CompositionLocal`s. It also initializes
   * the `YokeGlobal` object for non-composable access.
   */
  private fun generateYokeApplication(rootImplCn: ClassName, bindsInstanceParams: List<KSValueParameter>): FunSpec {
    val yokeGlobalCn = ClassName("io.github.yhpgi.yoke.di", "YokeGlobal")
    return FunSpec.builder("YokeApplication").addAnnotation(ClassName("androidx.compose.runtime", "Composable"))
      .addAnnotation(
        AnnotationSpec.builder(Suppress::class).addMember("%S", "DEPRECATION_ERROR").build()
      ).apply {
        bindsInstanceParams.forEach { param ->
          addParameter(param.name!!.asString(), param.type.resolve().toTypeName())
        }
      }.addParameter(
        "content", LambdaTypeName.get(returnType = UNIT).copy(
          annotations = listOf(AnnotationSpec.builder(ClassName("androidx.compose.runtime", "Composable")).build())
        )
      ).addCode(
        buildString {
          val args = bindsInstanceParams.joinToString(", ") { it.name!!.asString() }
          appendLine("|val rootContainer = %M { %T($args) }")
          appendLine("|val yokeContext = %M(rootContainer) { %T(rootContainer) }")
          appendLine("|")
          appendLine("|if (%T.context == null) {")
          appendLine("|  %T.context = yokeContext")
          appendLine("|  %T.resolver = GeneratedYokeResolver")
          appendLine("|}")
          appendLine("|")
          appendLine("|%T(")
          appendLine("|  %T provides rootContainer,")
          appendLine("|  %T provides yokeContext,")
          appendLine("|  %T provides GeneratedYokeResolver")
          appendLine("|) {")
          appendLine("|  content()")
          appendLine("|}")
          append("|")
        }.trimMargin(),
        MemberName("androidx.compose.runtime", "remember"),
        rootImplCn,
        MemberName("androidx.compose.runtime", "remember"),
        ClassName("io.github.yhpgi.yoke.di", "YokeContext"),
        yokeGlobalCn,
        yokeGlobalCn,
        yokeGlobalCn,
        ClassName("androidx.compose.runtime", "CompositionLocalProvider"),
        ClassName("io.github.yhpgi.yoke.di", "LocalDIContainer"),
        ClassName("io.github.yhpgi.yoke.di", "LocalYokeContext"),
        ClassName("io.github.yhpgi.yoke.di", "LocalYokeResolver")
      ).build()
  }

  /**
   * Detects cyclic dependencies in the graph.
   * If a cycle is found, it logs an error.
   */
  private fun detectCycles(graph: Map<DependencyKey, DependencyNode>) {
    val visiting = mutableSetOf<DependencyKey>()
    val visited = mutableSetOf<DependencyKey>()

    fun dfs(key: DependencyKey, path: List<DependencyKey>) {
      visiting.add(key)
      graph[key]?.dependencies?.forEach { dependencyKey ->
        if (dependencyKey in visiting) {
          val cyclePath = path.subList(path.indexOf(dependencyKey), path.size)
          logger.error("Cyclic dependency detected:\n" + cyclePath.joinToString("\n -> ") { "  ${it.typeName}" } + "\n -> ${dependencyKey.typeName}\n\n" + "To fix this cycle:\n" + "1. Use Provider<T> to break the cycle by injecting a lazy provider\n" + "2. Restructure your dependencies to avoid circular references\n" + "3. Consider using an intermediate factory or service locator pattern",
            graph[key]?.declaration)
          return
        }
        if (dependencyKey !in visited) {
          dfs(dependencyKey, path + dependencyKey)
        }
      }
      visiting.remove(key)
      visited.add(key)
    }

    graph.keys.forEach { key ->
      if (key !in visited) {
        dfs(key, listOf(key))
      }
    }
  }

  private fun KSClassDeclaration.getAssistedFactoryInterface(): KSType? {
    return declarations.filterIsInstance<KSClassDeclaration>().find { it.simpleName.asString() == "Factory" }
      ?.asStarProjectedType()
  }

  private fun KSClassDeclaration.isViewModel(): Boolean {
    return getAllSuperTypes().any {
      it.declaration.qualifiedName?.asString() == "androidx.lifecycle.ViewModel"
    }
  }
}

/**
 * A unique key for a dependency, composed of its type and an optional qualifier.
 * This is used as the key in the dependency graph map.
 *
 * @property typeName The fully qualified type name of the dependency.
 * @property qualifier An optional qualifier class name to disambiguate dependencies of the same type.
 */
data class DependencyKey(val typeName: TypeName, val qualifier: ClassName?) {
  /**
   * Generates a unique and valid property name for the provider of this dependency.
   * e.g., `(String, @Named("Greeting"))` -> `stringNamedGreetingProvider`.
   */
  fun toProviderName(): String {
    val base = typeName.rawClassName().simpleName.replaceFirstChar { it.lowercase() }
    val qual = qualifier?.simpleName?.replaceFirstChar { it.uppercase() } ?: ""
    return "${base}${qual}Provider"
  }
}

/**
 * A node in the dependency graph, representing a single injectable dependency.
 * It contains all the metadata needed to generate a provider for this dependency.
 *
 * @property key The unique key for this dependency.
 * @property declaration The KSP symbol (`KSClassDeclaration` or `KSFunctionDeclaration`) that defines this dependency.
 * @property dependencies A list of keys for the dependencies required by this node.
 * @property assistedParams A list of parameters that must be provided at runtime (for assisted injection).
 * @property bindings A list of interface keys that this node binds to.
 * @property scope The scope of this dependency, if any (e.g., `@Singleton`).
 * @property hostComponent The component this dependency is contributed to.
 * @property isBinding True if this node represents a `@Binds` declaration.
 * @property isFactory True if this node represents a factory for an assisted-injected type.
 * @property providesModule If this node is from a `@Provides` function, this holds the enclosing module.
 * @property isViewModel True if the provided type is a subclass of `androidx.lifecycle.ViewModel`.
 * @property needsSavedStateHandle True if provided type is a subclass of `androidx.lifecycle.SavedStateHandle`.
 */
data class DependencyNode(
  val key: DependencyKey,
  val declaration: KSDeclaration,
  val dependencies: List<DependencyKey>,
  val assistedParams: List<Param>,
  val bindings: List<DependencyKey>,
  val scope: ClassName?,
  val hostComponent: KSClassDeclaration,
  val isBinding: Boolean,
  val isFactory: Boolean,
  val providesModule: KSClassDeclaration? = null,
  val isViewModel: Boolean = false,
  val needsSavedStateHandle: Boolean = false
)

/**
 * Represents a parameter, typically for assisted injection.
 *
 * @property name The name of the parameter.
 * @property key The dependency key of the parameter.
 */
data class Param(val name: KSName, val key: DependencyKey)

/**
 * Extension function to extract a `@QualifiedBy` annotation value from a KSP element.
 */
private fun KSAnnotated.getQualifier(): ClassName? {
  return annotations.find { it.shortName.asString() == "QualifiedBy" }
    ?.let { (it.arguments.find { arg -> arg.name?.asString() == "value" }?.value as? KSType)?.declaration as? KSClassDeclaration }
    ?.toClassName()
}

/**
 * Extension function to extract a scope annotation (e.g., `@Singleton` or a custom `@Scope`) from a declaration.
 */
private fun KSDeclaration.getScope(): ClassName? {
  return annotations.find { it.shortName.asString() == "Singleton" }
    ?.let { ClassName("io.github.yhpgi.yoke.annotation", "Singleton") } ?: annotations.mapNotNull {
    (it.annotationType.resolve().declaration as? KSClassDeclaration)?.takeIf { decl ->
      decl.annotations.any { meta -> meta.shortName.asString() == "Scope" }
    }
  }.firstOrNull()?.toClassName()
}

private fun KSDeclaration.isComponent(): Boolean = hasAnnotation("YokeComponent")
private fun KSDeclaration.isSubcomponent(): Boolean = hasAnnotation("YokeSubcomponent")
private fun KSDeclaration.isInjectable(): Boolean = hasAnnotation("Injectable")
private fun KSDeclaration.isModule(): Boolean = hasAnnotation("Module")
private fun KSFunctionDeclaration.isProvides(): Boolean = hasAnnotation("Provides")
private fun KSFunctionDeclaration.isBinds(): Boolean = hasAnnotation("Binds")
private fun KSValueParameter.isAssisted(): Boolean = hasAnnotation("Assisted")
private fun KSValueParameter.isBindsInstance(): Boolean = hasAnnotation("BindsInstance")
private fun KSValueParameter.isSavedStateHandle(): Boolean {
  val typeName = type.resolve().toTypeName()
  return typeName.toString() == "androidx.lifecycle.SavedStateHandle"
}

private fun KSAnnotated.hasAnnotation(simpleName: String): Boolean {
  return annotations.any { it.shortName.asString() == simpleName }
}

/**
 * Converts a `KSValueParameter` into a `DependencyKey`.
 */
private fun KSValueParameter.toDependencyKey(parent: KSDeclaration, logger: KSPLogger): DependencyKey {
  val resolvedType = type.resolve()
  if (resolvedType.isError) {
    logger.error(
      "Unable to resolve type for parameter `${name?.asString()}` in `${parent.qualifiedName?.asString()}`.\n" + "Ensure all dependencies are properly imported and available in the classpath.",
      this
    )
  }
  return DependencyKey(resolvedType.toTypeName(), getQualifier())
}

/**
 * Gets the raw `ClassName` from a `TypeName`, unwrapping it if it's parameterized.
 */
private fun TypeName.rawClassName(): ClassName = when (this) {
  is ClassName -> this
  is ParameterizedTypeName -> rawType
  else -> error("Cannot get raw ClassName for type: $this")
}
