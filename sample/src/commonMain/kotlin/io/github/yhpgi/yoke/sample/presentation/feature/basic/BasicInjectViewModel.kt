package io.github.yhpgi.yoke.sample.presentation.feature.basic

import androidx.lifecycle.ViewModel
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.sample.di.AppComponent
import io.github.yhpgi.yoke.sample.domain.Greeter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The state for the Basic Injection screen.
 * @property greeting The greeting message to be displayed.
 */
data class BasicInjectState(
  val greeting: String
)

/**
 * The ViewModel for the [BasicInjectScreen].
 * Yoke injects a [Greeter] into this ViewModel's constructor automatically
 * because both classes are marked with `@Injectable` and `@ContributesTo(AppComponent::class)`.
 *
 * @property greeter The instance of [Greeter] injected by Yoke.
 */
@Injectable
@ContributesTo(AppComponent::class)
class BasicInjectViewModel(
  private val greeter: Greeter
) : ViewModel() {
  private val _state = MutableStateFlow(BasicInjectState(greeting = greeter.greet()))
  val state = _state.asStateFlow()
}
