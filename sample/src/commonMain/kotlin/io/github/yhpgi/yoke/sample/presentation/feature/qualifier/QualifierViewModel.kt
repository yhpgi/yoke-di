package io.github.yhpgi.yoke.sample.presentation.feature.qualifier

import androidx.lifecycle.ViewModel
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.annotation.QualifiedBy
import io.github.yhpgi.yoke.sample.di.GuestUser
import io.github.yhpgi.yoke.sample.di.PremiumUser
import io.github.yhpgi.yoke.sample.di.UserComponent
import io.github.yhpgi.yoke.sample.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Events for the [QualifierViewModel].
 */
sealed interface QualifierEvent {
  /** Event to load the usernames. */
  data object LoadUsernames : QualifierEvent
}

/**
 * The state for the Qualifier screen.
 * @property guestUsername The username from the guest repository.
 * @property premiumUsername The username from the premium repository.
 */
data class QualifierState(
  val guestUsername: String = "Loading...",
  val premiumUsername: String = "Loading..."
)

/**
 * The ViewModel for the [QualifierScreen].
 * This ViewModel is scoped to the `UserComponent` because all its dependencies
 * are also in that scope.
 *
 * It injects two different instances of the same interface, `UserRepository`,
 * by using the `@GuestUser` and `@PremiumUser` qualifiers.
 *
 * @property guestUserRepository The `UserRepository` implementation for guests.
 * @property premiumUserRepository The `UserRepository` implementation for premium users.
 */
@Injectable
@ContributesTo(UserComponent::class) // This ViewModel belongs to the UserComponent scope
class QualifierViewModel(
  @QualifiedBy(GuestUser::class) private val guestUserRepository: UserRepository,
  @QualifiedBy(PremiumUser::class) private val premiumUserRepository: UserRepository,
) : ViewModel() {
  private val _state = MutableStateFlow(QualifierState())
  val state = _state.asStateFlow()

  /**
   * Handles incoming events.
   * @param event The event to handle.
   */
  fun onEvent(event: QualifierEvent) {
    when (event) {
      QualifierEvent.LoadUsernames -> {
        _state.update {
          it.copy(
            guestUsername = guestUserRepository.getUsername(),
            premiumUsername = premiumUserRepository.getUsername()
          )
        }
      }
    }
  }
}
