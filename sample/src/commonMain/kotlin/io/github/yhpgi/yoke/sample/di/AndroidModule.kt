package io.github.yhpgi.yoke.sample.di

import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Module
import io.github.yhpgi.yoke.annotation.Provides

/**
 * A multiplatform module that provides platform-specific dependencies.
 * The `actual` implementations are provided in each source set (android, jvm, js).
 */
@Module
@ContributesTo(AppComponent::class)
expect object PlatformModule {
  /**
   * Provides the name of the current platform.
   */
  @Provides
  fun providePlatformName(): String
}
