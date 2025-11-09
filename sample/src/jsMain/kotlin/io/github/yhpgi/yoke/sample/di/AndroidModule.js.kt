package io.github.yhpgi.yoke.sample.di

import io.github.yhpgi.yoke.annotation.Module
import io.github.yhpgi.yoke.annotation.Provides

/**
 * The JavaScript-specific implementation of the [PlatformModule].
 */
@Module
actual object PlatformModule {
  /**
   * Provides the platform name for JavaScript.
   *
   * @return The string "JavaScript".
   */
  @Provides
  actual fun providePlatformName(): String {
    return "JavaScript"
  }
}
