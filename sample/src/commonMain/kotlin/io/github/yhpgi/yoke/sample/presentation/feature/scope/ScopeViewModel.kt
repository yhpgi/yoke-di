package io.github.yhpgi.yoke.sample.presentation.feature.scope

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.annotation.QualifiedBy
import io.github.yhpgi.yoke.sample.di.AppComponent
import io.github.yhpgi.yoke.sample.di.PremiumUser
import io.github.yhpgi.yoke.sample.di.UserComponent
import io.github.yhpgi.yoke.sample.domain.AnalyticsService
import io.github.yhpgi.yoke.sample.domain.AuthRepository
import io.github.yhpgi.yoke.sample.domain.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Events for the [ScopeViewModel].
 */
sealed interface ScopeEvent {
  /**
   * An event to perform a login.
   */
  data object Login : ScopeEvent

  /**
   * An event to perform a logout.
   */
  data object Logout : ScopeEvent
}

/**
 * The state for the Scope screen.
 *
 * @property isLoggedIn The login status of the user.
 * @property analyticsMessage A message from the analytics service.
 */
data class ScopeState(
  val isLoggedIn: Boolean = false,
  val analyticsMessage: String = ""
)

/**
 * The ViewModel for the [ScopeScreen].
 * This ViewModel resides in the `AppComponent` (application scope).
 * It injects `@Singleton` dependencies like [AuthRepository] and [AnalyticsService].
 *
 * @param authRepository The singleton authentication repository.
 * @param analyticsService The singleton analytics service.
 */
@Injectable
@ContributesTo(AppComponent::class)
class ScopeViewModel(
  private val authRepository: AuthRepository,
  private val analyticsService: AnalyticsService
) : ViewModel() {

  init {
    println("ScopeViewModel created: ${this.hashCode()}")
  }

  val state = authRepository.isLoggedIn.combine(
    MutableStateFlow(analyticsService.trackEvent("ViewModel call"))
  ) { isLoggedIn, analyticsMessage ->
    ScopeState(isLoggedIn, analyticsMessage)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Lazily,
    initialValue = ScopeState(
      isLoggedIn = authRepository.isLoggedIn.value,
      analyticsMessage = analyticsService.trackEvent("ViewModel call")
    )
  )

  /**
   * Handles UI events.
   *
   * @param event The event to handle.
   */
  fun onEvent(event: ScopeEvent) {
    println("onEvent called on ViewModel: ${this.hashCode()}, event: $event")
    viewModelScope.launch {
      when (event) {
        ScopeEvent.Login -> {
          println("login triggered")
          authRepository.login()
        }

        ScopeEvent.Logout -> {
          println("logout triggered")
          authRepository.logout()
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    println("ScopeViewModel cleared: ${this.hashCode()}")
  }
}

/**
 * The state for the user-scoped ViewModel.
 *
 * @property username The username.
 */
data class UserScopeState(val username: String)

/**
 * A ViewModel that is scoped to the `UserComponent`.
 * This ViewModel can only be created and injected when the `UserComponent` is active (i.e., when the user is logged in).
 * It injects the `UserRepository`, which is also scoped to the `UserComponent`.
 *
 * @param userRepository The user repository scoped to `UserComponent`.
 */
@Injectable
@ContributesTo(UserComponent::class)
class UserScopeViewModel(
  @QualifiedBy(PremiumUser::class) userRepository: UserRepository
) : ViewModel() {
  val state = MutableStateFlow(UserScopeState(username = userRepository.getUsername())).asStateFlow()
}
