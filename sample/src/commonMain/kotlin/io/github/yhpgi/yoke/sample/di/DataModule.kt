package io.github.yhpgi.yoke.sample.di

import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Module
import io.github.yhpgi.yoke.annotation.Provides
import io.github.yhpgi.yoke.annotation.Singleton
import io.github.yhpgi.yoke.sample.domain.AnalyticsService

/**
 * A DI module for providing dependencies whose constructors we cannot
 * annotate (e.g., from external libraries) or that require special configuration.
 * - `@Module`: Marks this object as a DI module.
 * - `@ContributesTo(AppComponent::class)`: Declares that all dependencies provided
 *   within this module will be available in the `AppComponent`.
 */
@Module
@ContributesTo(AppComponent::class)
object DataModule {
  /**
   * Provides an instance of [AnalyticsService].
   * - `@Provides`: Marks this function as a dependency provider. Yoke will call
   *   this function to get an instance of `AnalyticsService`.
   * - `@Singleton`: Ensures that Yoke will only call this function once and
   *   reuse the same instance for all subsequent injections.
   *
   * @return A singleton instance of [AnalyticsService].
   */
  @Provides
  @Singleton
  fun provideAnalyticsService(): AnalyticsService {
    return AnalyticsService("https://analytics.example.com/api")
  }
}
