package io.github.yhpgi.yoke.sample.di

import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Scope
import io.github.yhpgi.yoke.annotation.YokeSubcomponent
import io.github.yhpgi.yoke.di.DIContainer

/**
 * A custom scope annotation for dependencies whose lifecycle is tied
 * to the currently logged-in user session.
 * Annotations marked with `@Scope` can be used on components and dependencies.
 */
@Scope
@Retention(AnnotationRetention.SOURCE)
annotation class UserScope

/**
 * A subcomponent for the user session.
 * This component's lifecycle is shorter than `AppComponent`. It is created when
 * a user logs in and destroyed on logout.
 * - `@UserScope`: Assigns the scope for this component. All dependencies that are also
 *   annotated with `@UserScope` will be "singletons" within this component's scope.
 * - `@YokeSubcomponent`: Defines this interface as a subcomponent.
 * - `@ContributesTo(AppComponent::class)`: Links this subcomponent to its parent, `AppComponent`.
 *   This allows `UserComponent` to access dependencies from `AppComponent`.
 */
@UserScope
@YokeSubcomponent
@ContributesTo(AppComponent::class)
interface UserComponent : DIContainer
