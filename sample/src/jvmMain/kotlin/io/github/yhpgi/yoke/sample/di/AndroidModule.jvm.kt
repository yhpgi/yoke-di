package io.github.yhpgi.yoke.sample.di

import io.github.yhpgi.yoke.annotation.Module
import io.github.yhpgi.yoke.annotation.Provides

/**
 * The JVM-specific implementation of the [PlatformModule].
 */
@Module
actual object PlatformModule {
  /**
   * Provides the platform name for JVM Desktop.
   *
   * @return The string "JVM Desktop".
   */
  @Provides
  actual fun providePlatformName(): String {
    return "JVM Desktop"
  }
}
