package io.github.yhpgi.yoke.sample.presentation.feature.savedstate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.sample.di.AppComponent
import io.github.yhpgi.yoke.sample.domain.AnalyticsService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SavedStateEvent {
  data object Increment : SavedStateEvent
  data object Decrement : SavedStateEvent
}


@Injectable
@ContributesTo(AppComponent::class)
class SavedStateViewModel(
  private val analyticsService: AnalyticsService,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

  // Get a flow of the counter value from the SavedStateHandle, defaulting to 0
  val counter = savedStateHandle.getStateFlow("counter", 0).stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = savedStateHandle["counter"] ?: 0
  )

  fun onEvent(event: SavedStateEvent) {
    viewModelScope.launch {
      when (event) {
        SavedStateEvent.Increment -> {
          val newCount = counter.value + 1
          savedStateHandle["counter"] = newCount
          analyticsService.trackEvent("Counter Incremented to $newCount")
        }

        SavedStateEvent.Decrement -> {
          val newCount = counter.value - 1
          savedStateHandle["counter"] = newCount
          analyticsService.trackEvent("Counter Decremented to $newCount")
        }
      }
    }
  }
}
