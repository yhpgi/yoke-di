package io.github.yhpgi.yoke.sample.presentation.feature.provides

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.yhpgi.yoke.di.injectViewModel
import io.github.yhpgi.yoke.sample.presentation.shared.InfoCard

/**
 * A screen that demonstrates providing external dependencies using `@Provides`.
 */
@Composable
fun ProvidesScreen() {
  val viewModel = injectViewModel<ProvidesViewModel>()
  val state by viewModel.state.collectAsState()

  Scaffold { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
    ) {
      InfoCard(
        title = "External Dependencies (@Provides)",
        description = "Used inside a @Module object to provide dependencies that you don't own (e.g., from third-party libraries).",
        codeSnippet = """
                  // 1. Define the class you don't own
                  class AnalyticsService(private val url: String)

                  // 2. Create a Module
                  @Module
                  @ContributesTo(AppComponent::class)
                  object DataModule {
                    @Provides
                    fun provideAnalytics(): AnalyticsService {
                      return AnalyticsService("api.com")
                    }
                  }

                  // 3. Inject it with the DSL
                  val service = inject<AnalyticsService>()
                """.trimIndent()
      )
      Spacer(Modifier.height(16.dp))
      Text("Live Demo:", style = MaterialTheme.typography.titleMedium)
      Spacer(Modifier.height(8.dp))
      Text(state.trackedEventMessage)
    }
  }
}
