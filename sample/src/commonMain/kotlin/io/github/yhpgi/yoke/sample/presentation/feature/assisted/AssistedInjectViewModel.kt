package io.github.yhpgi.yoke.sample.presentation.feature.assisted

import androidx.lifecycle.ViewModel
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.sample.di.AppComponent
import io.github.yhpgi.yoke.sample.domain.ListItemFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Events that can be triggered on the [AssistedInjectViewModel].
 */
sealed interface AssistedInjectEvent {
  /**
   * An event to format an item.
   */
  data object FormatItem : AssistedInjectEvent
}

/**
 * The state for the Assisted Injection screen.
 * @property itemId The ID of the item to be formatted.
 * @property formattedText The resulting formatted text.
 */
data class AssistedInjectState(
  val itemId: Int = 101,
  val formattedText: String = "Loading..."
)

/**
 * The ViewModel for the [AssistedInjectScreen].
 * It demonstrates how to inject a factory (`ListItemFormatter.Factory`)
 * and use it to create `ListItemFormatter` instances with runtime parameters.
 *
 * @property formatterFactory The factory injected by Yoke.
 */
@Injectable
@ContributesTo(AppComponent::class)
class AssistedInjectViewModel(
  private val formatterFactory: ListItemFormatter.Factory
) : ViewModel() {
  private val _state = MutableStateFlow(AssistedInjectState())
  val state = _state.asStateFlow()

  /**
   * Handles incoming events.
   * @param event The event to handle.
   */
  fun onEvent(event: AssistedInjectEvent) {
    when (event) {
      AssistedInjectEvent.FormatItem -> {
        // Use the factory to create an instance with runtime parameters.
        val formatter = formatterFactory.create("Item", _state.value.itemId)
        _state.update { it.copy(formattedText = formatter.format()) }
      }
    }
  }
}
