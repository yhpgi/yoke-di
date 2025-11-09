package io.github.yhpgi.yoke.sample.data

import io.github.yhpgi.yoke.annotation.Binds
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.annotation.Singleton
import io.github.yhpgi.yoke.sample.di.AppComponent
import io.github.yhpgi.yoke.sample.domain.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The concrete implementation of [AuthRepository].
 * This class is marked as `@Singleton` so that there will be only one instance
 * throughout the application. `@Binds` is used to tell Yoke to provide this
 * instance when `AuthRepository` is injected.
 */
@Singleton
@Injectable
@Binds(to = AuthRepository::class)
@ContributesTo(AppComponent::class)
class AuthRepositoryImpl : AuthRepository {
  private val _isLoggedIn = MutableStateFlow(false)
  override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

  /**
   * Simulates a login process.
   */
  override suspend fun login() {
    delay(500)
    _isLoggedIn.value = true
  }

  /**
   * Simulates a logout process.
   */
  override suspend fun logout() {
    delay(200)
    _isLoggedIn.value = false
  }
}
