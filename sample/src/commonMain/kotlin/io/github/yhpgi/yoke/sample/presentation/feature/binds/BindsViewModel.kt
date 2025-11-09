package io.github.yhpgi.yoke.sample.presentation.feature.binds

import androidx.lifecycle.ViewModel
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.sample.di.AppComponent
import io.github.yhpgi.yoke.sample.domain.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The state for the Binds screen.
 * @property message The message obtained from the repository.
 */
data class BindsState(
  val message: String
)

/**
 * The ViewModel for the [BindsScreen].
 * This ViewModel requests a [MessageRepository] (an interface) in its constructor.
 * Yoke knows to provide the [io.github.yhpgi.yoke.sample.data.MessageRepositoryImpl]
 * because that implementation class is marked with `@Binds(to = MessageRepository::class)`.
 *
 * @property messageRepository The injected instance of the [MessageRepository] implementation.
 */
@Injectable
@ContributesTo(AppComponent::class)
class BindsViewModel(
  private val messageRepository: MessageRepository
) : ViewModel() {
  private val _state = MutableStateFlow(BindsState(message = messageRepository.getMessage()))
  val state = _state.asStateFlow()
}
