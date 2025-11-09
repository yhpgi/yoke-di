package io.github.yhpgi.yoke.sample.di

import io.github.yhpgi.yoke.annotation.Module
import io.github.yhpgi.yoke.annotation.Provides

/**
 * The WebAssembly-specific implementation of the [PlatformModule].
 */
@Module
actual object PlatformModule {
  /**
   * Provides the platform name for WebAssembly.
   *
   * @return The string "WebAssembly".
   */
  @Provides
  actual fun providePlatformName(): String {
    return "WebAssembly"
  }
}
