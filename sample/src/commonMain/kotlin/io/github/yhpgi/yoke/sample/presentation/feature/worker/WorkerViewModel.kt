package io.github.yhpgi.yoke.sample.presentation.feature.worker

import androidx.lifecycle.ViewModel
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.sample.di.AppComponent
import io.github.yhpgi.yoke.sample.worker.WorkerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Events for the [WorkerViewModel].
 */
sealed interface WorkerEvent {
  /**
   * Event to execute the background worker task.
   */
  data object ExecuteWorker : WorkerEvent
}

/**
 * The state for the [WorkerScreen].
 * @property workerResult The result message from the worker execution.
 * @property platformName The name of the current platform.
 */
data class WorkerState(
  val workerResult: String = "Press button to execute worker",
  val platformName: String = ""
)

/**
 * The ViewModel for the [WorkerScreen].
 * @param workerManager The manager responsible for executing worker tasks.
 * @param platformName The name of the current platform, injected via [PlatformModule].
 */
@Injectable
@ContributesTo(AppComponent::class)
class WorkerViewModel(
  private val workerManager: WorkerManager,
  platformName: String
) : ViewModel() {
  private val _state = MutableStateFlow(WorkerState(platformName = platformName))
  val state = _state.asStateFlow()

  /**
   * Handles incoming UI events.
   * @param event The event to process.
   */
  fun onEvent(event: WorkerEvent) {
    when (event) {
      WorkerEvent.ExecuteWorker -> {
        val result = workerManager.executeWorker()
        _state.update { it.copy(workerResult = result) }
      }
    }
  }
}
