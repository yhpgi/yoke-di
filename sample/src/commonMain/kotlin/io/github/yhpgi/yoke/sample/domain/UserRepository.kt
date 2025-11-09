package io.github.yhpgi.yoke.sample.domain

/**
 * An interface for a repository that provides user information.
 * Used to demonstrate the Qualifier feature, as there are multiple
 * implementations of this interface.
 */
interface UserRepository {
  /**
   * Gets the username.
   * @return The username string.
   */
  fun getUsername(): String
}
