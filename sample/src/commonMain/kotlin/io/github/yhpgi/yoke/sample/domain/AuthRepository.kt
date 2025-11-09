package io.github.yhpgi.yoke.sample.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * An interface defining the contract for authentication management.
 * In DI, it is better to depend on interfaces (abstractions) rather than concrete implementations.
 */
interface AuthRepository {
  /**
   * A [StateFlow] that emits the current login status of the user.
   * `true` if logged in, `false` otherwise.
   */
  val isLoggedIn: StateFlow<Boolean>

  /**
   * Executes the login process.
   */
  suspend fun login()

  /**
   * Executes the logout process.
   */
  suspend fun logout()
}
