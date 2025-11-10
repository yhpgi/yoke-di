package io.github.yhpgi.yoke.di

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.reflect.KClass

/**
 * A marker interface for a Yoke dependency injection container.
 * Both `@YokeComponent` and `@YokeSubcomponent` interfaces must extend this.
 */
interface DIContainer

/**
 * A provider for a dependency of type [T].
 * This is the fundamental building block for dependency injection.
 *
 * @param T The type of the dependency.
 */
interface Provider<out T> {
  /**
   * Provides an instance of [T].
   */
  fun get(): T
}

/**
 * A provider that creates an instance of [T] on every call to [get].
 * This corresponds to an unscoped dependency.
 *
 * @param T The type of the dependency.
 * @param factory The lambda function that creates the instance.
 */
class LazyProvider<out T>(
  private val factory: () -> T
) : Provider<T> {
  override fun get(): T = factory()
}

/**
 * A provider that creates an instance of [T] only once and caches it.
 * Subsequent calls to [get] return the cached instance. This corresponds
 * to a scoped dependency (e.g., `@Singleton`).
 *
 * @param T The type of the dependency.
 * @param provider The underlying provider to delegate the initial creation to.
 */
class SingletonProvider<out T>(
  private val provider: Provider<T>
) : Provider<T> {
  private val value: T by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { provider.get() }
  override fun get(): T = value
}

/**
 * A `CompositionLocal` that holds the root [DIContainer] for the current composition.
 * Access this via `LocalDIContainer.current`.
 */
val LocalDIContainer: ProvidableCompositionLocal<DIContainer> = compositionLocalOf {
  error("DIContainer not found. Wrap your app in YokeApplication { }")
}

/**
 * A `CompositionLocal` that holds the current [YokeContext].
 * The context manages subcomponent lifecycles.
 */
val LocalYokeContext: ProvidableCompositionLocal<YokeContext> = compositionLocalOf {
  error("YokeContext not found. Wrap your app in YokeApplication { }")
}

/**
 * The core resolver interface for Yoke. It provides access to any `Provider<T>`
 * within the dependency graph.
 */
interface YokeResolver {
  /**
   * A Composable function to retrieve a provider for a given type and optional qualifier.
   *
   * @param kClass The `KClass` of the dependency to resolve.
   * @param qualifier An optional qualifier class to disambiguate dependencies.
   * @return A `Provider<T>` for the requested dependency.
   */
  @Composable
  fun <T : Any> getProvider(
    kClass: KClass<T>, qualifier: KClass<*>?
  ): Provider<T>

  /**
   * A non-Composable function to retrieve a provider. Requires an explicit [YokeContext].
   *
   * @param context The current [YokeContext].
   * @param kClass The `KClass` of the dependency to resolve.
   * @param qualifier An optional qualifier class to disambiguate dependencies.
   * @return A `Provider<T>` for the requested dependency.
   */
  fun <T : Any> getProvider(
    context: YokeContext, kClass: KClass<T>, qualifier: KClass<*>?
  ): Provider<T>
}

/**
 * A `CompositionLocal` that holds the current [YokeResolver].
 */
val LocalYokeResolver: ProvidableCompositionLocal<YokeResolver> = compositionLocalOf {
  error("YokeResolver not found. Wrap your app in YokeApplication { }")
}

/**
 * Manages the lifecycle of subcomponents. It ensures that only one instance of a
 * given subcomponent exists at a time and handles their creation and destruction.
 *
 * @property root The root [DIContainer] of the application.
 */
class YokeContext(val root: DIContainer) {
  private val subcomponents = mutableMapOf<KClass<*>, DIContainer>()

  /**
   * Gets an existing subcomponent instance or creates a new one if it doesn't exist.
   *
   * @param kClass The `KClass` of the subcomponent to get or create.
   * @param factory A lambda function to create the subcomponent if needed.
   * @return The instance of the subcomponent.
   */
  fun <T : DIContainer> getOrCreate(kClass: KClass<T>, factory: (DIContainer) -> T): T {
    @Suppress("UNCHECKED_CAST") return subcomponents.getOrPut(kClass) { factory(root) } as T
  }

  /**
   * Destroys a subcomponent instance, removing it from the context.
   * This is typically called when a scope ends (e.g., a user logs out).
   *
   * @param kClass The `KClass` of the subcomponent to destroy.
   */
  fun destroySubcomponent(kClass: KClass<*>) {
    subcomponents.remove(kClass)
  }

  companion object {
    /**
     * Provides convenient access to the current `YokeContext` within a Composable function.
     */
    val current: YokeContext @Composable get() = LocalYokeContext.current
  }
}

/**
 * This is a stub for the `YokeApplication` composable. The actual implementation is generated
 * by the KSP processor based on your `@YokeEntryPoint` component. If you see this error,
 * ensure that KSP is configured correctly and your project has been built.
 */
@Composable
@Deprecated(
  "YokeApplication stub - KSP has not generated the real implementation yet. Build the project to generate it.",
  level = DeprecationLevel.WARNING
)
fun YokeApplication(content: @Composable () -> Unit) {
  error("YokeApplication is not generated yet. Please build your project with KSP enabled.")
}

/**
 * A DSL marker for Yoke's injection and scoping APIs to ensure proper nesting and usage.
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
annotation class YokeInjectDsl

/**
 * The base class for Yoke's DSL builders, providing common functionality like qualifiers.
 */
@YokeInjectDsl
abstract class YokeInjectionBuilder {
  @PublishedApi
  internal var qualifier: KClass<*>? = null

  /**
   * Specifies a qualifier for this injection, used to disambiguate between different
   * bindings of the same type.
   *
   * @param qualifier The qualifier annotation's `KClass`.
   */
  fun qualifiedBy(qualifier: KClass<*>) {
    this.qualifier = qualifier
  }
}

/**
 * A builder for configuring dependency injection with the `inject` function.
 *
 * @param T The type of the dependency being injected.
 */
@YokeInjectDsl
class InjectBuilder<T : Any> @PublishedApi internal constructor(
  private val kClass: KClass<T>
) : YokeInjectionBuilder() {

  @Composable
  @PublishedApi
  internal fun build(resolver: YokeResolver): T {
    val provider = resolver.getProvider(kClass, qualifier)
    return remember(provider, qualifier) { provider.get() }
  }
}

/**
 * A builder for configuring ViewModel injection with the `injectViewModel` function.
 *
 * @param T The type of the `ViewModel` being injected.
 */
@YokeInjectDsl
class ViewModelBuilder<T : ViewModel> @PublishedApi internal constructor(
  private val kClass: KClass<T>
) : YokeInjectionBuilder() {

  private var viewModelStoreOwner: ViewModelStoreOwner? = null

  /**
   * Specifies a custom `ViewModelStoreOwner` to scope the ViewModel's lifecycle.
   * If not provided, the local `ViewModelStoreOwner` from the composition will be used.
   *
   * @param owner The custom `ViewModelStoreOwner`.
   */
  fun scopedTo(owner: ViewModelStoreOwner) {
    viewModelStoreOwner = owner
  }

  @Composable
  @PublishedApi
  internal fun build(resolver: YokeResolver): T {
    val owner = viewModelStoreOwner ?: checkNotNull(LocalViewModelStoreOwner.current) {
      "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }

    val provider = resolver.getProvider(kClass, qualifier)

    return viewModel(
      modelClass = kClass, viewModelStoreOwner = owner, factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: KClass<VM>, extras: CreationExtras): VM {
          return provider.get() as VM
        }
      })
  }
}

/**
 * The main DSL entry point for dependency injection in Composable functions.
 *
 * Example:
 * ```
 * // Simple injection
 * val repository = inject<Repository>()
 *
 * // Injection with a qualifier
 * val premiumRepository = inject<Repository> {
 *   qualifiedBy(PremiumUser::class)
 * }
 * ```
 * @param T The type of the dependency to inject.
 * @param builder A lambda to configure the injection, e.g., by specifying a qualifier.
 * @return An instance of the requested dependency, remembered across recompositions.
 */
@Composable
inline fun <reified T : Any> inject(
  noinline builder: InjectBuilder<T>.() -> Unit = {}
): T {
  val resolver = LocalYokeResolver.current
  val injectionBuilder = remember { InjectBuilder(T::class) }
  injectionBuilder.builder()
  return injectionBuilder.build(resolver)
}

/**
 * The main DSL entry point for ViewModel injection in Composable functions.
 *
 * Example:
 * ```
 * // Simple ViewModel injection
 * val myViewModel = injectViewModel<MyViewModel>()
 *
 * // ViewModel injection with options
 * val userViewModel = injectViewModel<UserViewModel> {
 *   qualifiedBy(SomeQualifier::class)
 *   scopedTo(customViewModelStoreOwner)
 * }
 * ```
 * @param T The type of the `ViewModel` to inject.
 * @param builder A lambda to configure the injection, e.g., specifying a qualifier or scope.
 * @return An instance of the requested `ViewModel`, correctly scoped to its `ViewModelStoreOwner`.
 */
@Composable
inline fun <reified T : ViewModel> injectViewModel(
  noinline builder: ViewModelBuilder<T>.() -> Unit = {}
): T {
  val resolver = LocalYokeResolver.current
  val viewModelBuilder = remember { ViewModelBuilder(T::class) }
  viewModelBuilder.builder()
  return viewModelBuilder.build(resolver)
}

/**
 * A DSL entry point for non-composable dependency injection. Use this in background
 * threads, services, workers, or other parts of your app outside the composition.
 *
 * Requires `YokeApplication` to have been composed once to initialize the global context.
 *
 * Example:
 * ```
 * // In a background worker
 * val analytics = injectGlobal<AnalyticsService>()
 * analytics.trackEvent("Worker task started")
 * ```
 * @param T The type of the dependency to inject.
 * @param builder A lambda to configure the injection, e.g., by specifying a qualifier.
 * @return A new instance of the requested dependency.
 */
inline fun <reified T : Any> injectGlobal(
  builder: InjectBuilder<T>.() -> Unit = {}
): T {
  val context = YokeGlobal.context
    ?: error("Yoke is not initialized. Make sure to wrap your Composable app in YokeApplication { ... } or call AndroidYoke.initialize() in your Application class.")
  val resolver = YokeGlobal.resolver ?: error("Yoke resolver not found. Initialization failed.")

  val injectionBuilder = InjectBuilder(T::class)
  injectionBuilder.builder()

  val provider = resolver.getProvider(context, T::class, injectionBuilder.qualifier)
  return provider.get()
}

/**
 * A builder for managing the lifecycle and content of a scoped subcomponent.
 * Used with the `scope` function.
 *
 * @param T The type of the subcomponent (`DIContainer`) being managed.
 */
@YokeInjectDsl
class ScopeBuilder<T : DIContainer> @PublishedApi internal constructor(
  private val component: KClass<T>
) {
  private var activeCondition: () -> Boolean = { true }
  private var activeContent: @Composable (T) -> Unit = {}
  private var inactiveContent: @Composable () -> Unit = {}
  private var onActivateCallback: (() -> Unit)? = null
  private var onDeactivateCallback: (() -> Unit)? = null

  /**
   * Sets the condition that determines if the component's scope should be active.
   * @param condition A lambda returning `true` if the scope is active, `false` otherwise.
   */
  fun active(condition: () -> Boolean) {
    activeCondition = condition
  }

  /**
   * Sets the Composable content to display when the scope is active.
   * Dependencies from the subcomponent can be injected within this content.
   * @param content The Composable content for the active state.
   */
  fun whenActive(content: @Composable (T) -> Unit) {
    activeContent = content
  }

  /**
   * Sets the Composable content to display when the scope is inactive.
   * @param content The Composable content for the inactive state.
   */
  fun whenInactive(content: @Composable () -> Unit) {
    inactiveContent = content
  }

  /**
   * Sets a callback to be invoked when the scope transitions from inactive to active.
   * @param callback The callback to run on activation.
   */
  fun onActivate(callback: () -> Unit) {
    onActivateCallback = callback
  }

  /**
   * Sets a callback to be invoked when the scope transitions from active to inactive.
   * @param callback The callback to run on deactivation.
   */
  fun onDeactivate(callback: () -> Unit) {
    onDeactivateCallback = callback
  }

  @Composable
  @PublishedApi
  internal fun Build(resolver: YokeResolver, context: YokeContext) {
    val isActive = activeCondition()

    if (isActive) {
      val provider = resolver.getProvider(component, null)

      @Suppress("UNCHECKED_CAST")
      val componentInstance = remember(provider) { provider.get() }
      activeContent(componentInstance)
    } else {
      inactiveContent()
    }

    DisposableEffect(isActive) {
      if (isActive) {
        onActivateCallback?.invoke()
      }

      onDispose {
        if (!isActive) {
          onDeactivateCallback?.invoke()
          context.destroySubcomponent(component)
        }
      }
    }
  }
}

/**
 * The DSL entry point for managing a scoped subcomponent's lifecycle and content.
 * It automatically creates the component when active and destroys it when inactive.
 *
 * Example:
 * ```
 * scope<UserComponent> {
 *   active { isLoggedIn }
 *
 *   whenActive {
 *     val userViewModel = injectViewModel<UserViewModel>()
 *     Text("Hello, ${userViewModel.username}")
 *   }
 *
 *   whenInactive {
 *     Text("Please log in")
 *   }
 *
 *   onActivate { println("User session started") }
 *   onDeactivate { println("User session ended") }
 * }
 * ```
 * @param T The type of the subcomponent to manage.
 * @param builder A lambda to configure the scope's behavior.
 */
@Composable
inline fun <reified T : DIContainer> Scope(
  noinline builder: ScopeBuilder<T>.() -> Unit
) {
  val resolver = LocalYokeResolver.current
  val context = YokeContext.current
  val scopeBuilder = remember(T::class) { ScopeBuilder(T::class) }
  scopeBuilder.builder()
  scopeBuilder.Build(resolver, context)
}
