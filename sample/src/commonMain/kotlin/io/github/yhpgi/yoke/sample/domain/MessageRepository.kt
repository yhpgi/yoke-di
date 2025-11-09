package io.github.yhpgi.yoke.sample.domain

/**
 * An interface for a repository that provides a message.
 * Used to demonstrate the `@Binds` feature.
 */
interface MessageRepository {
  /**
   * Gets a message.
   * @return A message string.
   */
  fun getMessage(): String
}
