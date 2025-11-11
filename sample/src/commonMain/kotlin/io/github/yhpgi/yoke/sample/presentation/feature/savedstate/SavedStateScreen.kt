package io.github.yhpgi.yoke.sample.presentation.feature.savedstate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.yhpgi.yoke.di.injectViewModel
import io.github.yhpgi.yoke.sample.presentation.shared.InfoCard

/**
 * A screen that demonstrates automatic `SavedStateHandle` injection.
 */
@Composable
fun SavedStateScreen() {
  val viewModel = injectViewModel<SavedStateViewModel>()
  val counter by viewModel.counter.collectAsState()

  Scaffold { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      InfoCard(
        title = "Automatic SavedStateHandle Injection",
        description = "Yoke automatically injects `SavedStateHandle` into your ViewModels if it's present in the constructor. No manual factories or extra annotations are needed.",
        codeSnippet = """
                  @Injectable
                  @ContributesTo(AppComponent::class)
                  class SavedStateViewModel(
                    private val analytics: AnalyticsService,
                    // Yoke automatically provides this handle
                    private val savedStateHandle: SavedStateHandle
                  ) : ViewModel() {
                    val counter = savedStateHandle.getStateFlow("counter", 0)

                    fun increment() {
                      savedStateHandle["counter"] = counter.value + 1
                    }
                  }

                  // At the call site, nothing changes!
                  val viewModel = injectViewModel<SavedStateViewModel>()
                """.trimIndent()
      )
      Spacer(Modifier.height(32.dp))
      Text("Live Demo:", style = MaterialTheme.typography.titleMedium)
      Spacer(Modifier.height(8.dp))
      Text(
        "Try rotating the screen or putting the app in the background. The counter state will be preserved by the SavedStateHandle.",
        style = MaterialTheme.typography.bodySmall,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
      Spacer(Modifier.height(16.dp))

      Text("Counter: $counter", style = MaterialTheme.typography.headlineLarge)
      Spacer(Modifier.height(16.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = { viewModel.onEvent(SavedStateEvent.Decrement) }) { Text("-") }
        Button(onClick = { viewModel.onEvent(SavedStateEvent.Increment) }) { Text("+") }
      }
    }
  }
}
