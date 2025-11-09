package io.github.yhpgi.yoke.sample.domain

import io.github.yhpgi.yoke.annotation.Assisted
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.sample.di.AppComponent

/**
 * This class demonstrates "Assisted Injection".
 * It requires some dependencies from the DI graph (`analyticsService`) and
 * some parameters that must be provided at creation time (`prefix`, `id`).
 *
 * @property analyticsService A dependency provided by Yoke.
 * @property prefix A parameter provided at runtime via the factory.
 * @property id A parameter provided at runtime via the factory.
 */
@Injectable
@ContributesTo(AppComponent::class)
class ListItemFormatter(
  private val analyticsService: AnalyticsService,
  @Assisted private val prefix: String,
  @Assisted private val id: Int,
) {
  /**
   * Formats an item into a string.
   * @return The formatted string.
   */
  fun format(): String {
    analyticsService.trackEvent("formatListItem")
    return "$prefix: Item ID #$id has been formatted."
  }

  /**
   * A factory interface for [ListItemFormatter].
   * Yoke will generate the implementation for this factory automatically.
   * You should inject `ListItemFormatter.Factory` instead of `ListItemFormatter` itself.
   */
  interface Factory {
    /**
     * Creates a new instance of [ListItemFormatter].
     * @param prefix The value for the `@Assisted` `prefix` parameter.
     * @param id The value for the `@Assisted` `id` parameter.
     * @return A new instance of [ListItemFormatter].
     */
    fun create(prefix: String, id: Int): ListItemFormatter
  }
}
