package io.github.yhpgi.yoke.sample.presentation.feature.provides

import androidx.lifecycle.ViewModel
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.sample.di.AppComponent
import io.github.yhpgi.yoke.sample.domain.AnalyticsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The state for the Provides screen.
 * @property trackedEventMessage The message generated after tracking an event.
 */
data class ProvidesState(
  val trackedEventMessage: String
)

/**
 * The ViewModel for the [ProvidesScreen].
 * This ViewModel requires an [AnalyticsService]. Since `AnalyticsService` does not have an
 * `@Injectable` annotation, Yoke will look for it inside a module.
 * It finds it in `DataModule.provideAnalyticsService()`, which is annotated with `@Provides`.
 *
 * @param analyticsService The instance of [AnalyticsService] provided by `DataModule`.
 */
@Injectable
@ContributesTo(AppComponent::class)
class ProvidesViewModel(
  analyticsService: AnalyticsService
) : ViewModel() {
  private val _state = MutableStateFlow(
    ProvidesState(trackedEventMessage = analyticsService.trackEvent("ProvidesScreenVisited"))
  )
  val state = _state.asStateFlow()
}
