package io.github.yhpgi.yoke.sample.presentation.feature.home

import androidx.lifecycle.ViewModel
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.sample.di.AppComponent
import io.github.yhpgi.yoke.sample.presentation.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents a feature displayed on the [HomeScreen].
 *
 * @property title The name of the feature.
 * @property screen The navigation destination for the feature.
 */
data class Feature(val title: String, val screen: Screen)

/**
 * The state for the [HomeScreen].
 *
 * @property features A list of features to be displayed.
 */
data class HomeState(
  val features: List<Feature> = listOf(
    Feature("Basic Injection (@Injectable)", Screen.BasicInject),
    Feature("Interface Binding (@Binds)", Screen.Binds),
    Feature("External Dependencies (@Provides)", Screen.Provides),
    Feature("Scoping & ViewModels", Screen.Scopes),
    Feature("Qualifiers (@QualifiedBy)", Screen.Qualifiers),
    Feature("Assisted Injection (@Assisted)", Screen.AssistedInject),
    Feature("Non-Composable Injection", Screen.Worker),
    Feature("Complete DSL API", Screen.Dsl),
    Feature("Saved State Handle Injection", Screen.SavedState)
  )
)

/**
 * The ViewModel for the [HomeScreen].
 */
@Injectable
@ContributesTo(AppComponent::class)
class HomeViewModel : ViewModel() {
  private val _state = MutableStateFlow(HomeState())
  val state = _state.asStateFlow()
}
