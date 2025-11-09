package io.github.yhpgi.yoke.sample.domain

/**
 * An example class representing a third-party analytics service.
 * Since we don't "own" this code (e.g., it's from a library), we cannot
 * add `@Injectable` to its constructor.
 * Therefore, we must provide it via a `@Module` and `@Provides`.
 *
 * @property apiUrl The API endpoint URL for the analytics service.
 * @see io.github.yhpgi.yoke.sample.di.DataModule
 */
class AnalyticsService(private val apiUrl: String) {
  private var eventCount = 0

  /**
   * Tracks an event.
   * @param name The name of the event to track.
   * @return A tracking confirmation message.
   */
  fun trackEvent(name: String): String {
    eventCount++
    return "Event '$name' tracked via $apiUrl. Total events: $eventCount"
  }
}
