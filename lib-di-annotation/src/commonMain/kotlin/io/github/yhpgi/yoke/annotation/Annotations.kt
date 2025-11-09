package io.github.yhpgi.yoke.annotation

import kotlin.reflect.KClass

/**
 * Marks an interface as the root component for the dependency graph.
 * There should be exactly one `@YokeEntryPoint` in the project.
 * Yoke will generate a `YokeApplication` Composable based on this entry point.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class YokeEntryPoint

/**
 * Defines a component in the dependency injection graph.
 * Components are interfaces that group together modules and define scopes.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class YokeComponent

/**
 * Defines a subcomponent, which is a component with a shorter lifecycle
 * that is a child of another component.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class YokeSubcomponent

/**
 * Specifies which component a module or dependency should be installed in.
 *
 * @property scope The component class to contribute to.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ContributesTo(val scope: KClass<*>)

/**
 * Marks a class's constructor for dependency injection. Yoke will know how to
 * create instances of this class and satisfy its dependencies.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Injectable

/**
 * Marks an object or class as a DI module. Modules are used to provide
 * dependencies that cannot be constructor-injected, such as instances from
 * external libraries or classes that require complex initialization.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Module

/**
 * Marks a function inside a `@Module` to be a provider of a dependency.
 * The function's return type determines the type of the provided dependency.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Provides

/**
 * Binds an implementation class to an interface. This is used on an `@Injectable`
 * class or on an abstract function in a `@Module` to tell Yoke which implementation
 * to provide when an interface is requested.
 *
 * @property to The interface `KClass` that this implementation binds to.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Binds(val to: KClass<*>)

/**
 * A scope annotation that indicates a dependency should have a single instance
 * for the lifetime of the component it's scoped to. When used with the root
 * component, it behaves as an application-level singleton.
 */
@Scope
@Retention(AnnotationRetention.SOURCE)
annotation class Singleton

/**
 * Meta-annotation used to create custom scope annotations.
 * For example, `@UserScope` could be defined by annotating it with `@Scope`.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Scope

/**
 * Specifies a qualifier annotation to be used for a dependency or at an injection site.
 * This is necessary when multiple providers exist for the same type.
 *
 * @property value The qualifier annotation class.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class QualifiedBy(val value: KClass<out Annotation>)

/**
 * Meta-annotation used to create custom qualifier annotations.
 * For example, `@LoggedInUser` could be defined by annotating it with `@YokeQualifier`.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class YokeQualifier

/**
 * Marks a parameter of an `@Injectable` class's constructor that is not provided
 * by the DI graph, but instead must be supplied at runtime by the user via a factory.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Assisted
