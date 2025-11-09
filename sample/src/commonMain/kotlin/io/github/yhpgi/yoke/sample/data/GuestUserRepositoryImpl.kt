package io.github.yhpgi.yoke.sample.data

import io.github.yhpgi.yoke.annotation.Binds
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.annotation.QualifiedBy
import io.github.yhpgi.yoke.sample.di.GuestUser
import io.github.yhpgi.yoke.sample.di.UserComponent
import io.github.yhpgi.yoke.sample.di.UserScope
import io.github.yhpgi.yoke.sample.domain.UserRepository

/**
 * The implementation of [UserRepository] for guest users.
 * - `@UserScope`: This instance will live as long as the `UserComponent` is active (user session).
 * - `@ContributesTo(UserComponent::class)`: This dependency is only available within the `UserComponent` scope.
 * - `@QualifiedBy(GuestUser::class)`: A qualifier to distinguish it from other `UserRepository` implementations.
 * - `@Binds`: Binds this implementation to the `UserRepository` interface.
 */
@Injectable
@UserScope
@ContributesTo(UserComponent::class)
@QualifiedBy(GuestUser::class)
@Binds(to = UserRepository::class)
class GuestUserRepositoryImpl : UserRepository {
  override fun getUsername(): String = "Guest User"
}
