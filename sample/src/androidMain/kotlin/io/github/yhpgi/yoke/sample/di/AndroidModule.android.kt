package io.github.yhpgi.yoke.sample.di

import android.content.Context
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Module
import io.github.yhpgi.yoke.annotation.Provides
import io.github.yhpgi.yoke.annotation.Singleton
import io.github.yhpgi.yoke.di.AndroidYoke

/**
 * The Android-specific implementation of the [PlatformModule].
 * This module provides dependencies that are specific to the Android platform.
 */
@Module
@ContributesTo(AppComponent::class)
actual object PlatformModule {

  /**
   * Provides the Android application `Context` as a dependency.
   *
   * @return The application context.
   */
  @Provides
  @Singleton
  fun provideApplicationContext(): Context {
    return AndroidYoke.getApplicationContext()
  }

  /**
   * Provides the platform name for Android.
   *
   * @return The string "Android".
   */
  @Provides
  actual fun providePlatformName(): String {
    return "Android"
  }
}
