package io.github.yhpgi.yoke.sample.presentation.feature.assisted

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.yhpgi.yoke.di.injectViewModel
import io.github.yhpgi.yoke.sample.presentation.shared.InfoCard

/**
 * A screen that demonstrates the Assisted Injection feature using the DSL.
 */
@Composable
fun AssistedInjectScreen() {
  val viewModel = injectViewModel<AssistedInjectViewModel>()
  val state by viewModel.state.collectAsState()

  LaunchedEffect(Unit) {
    viewModel.onEvent(AssistedInjectEvent.FormatItem)
  }

  Scaffold { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
      InfoCard(
        title = "Assisted Injection (@Assisted)",
        description = "For factory-like patterns. Use this when a class needs some dependencies from the DI graph and others provided at creation time.",
        codeSnippet = """
                  // 1. Annotate the assisted parameters
                  @Injectable
                  class ListItemFormatter(
                    private val analytics: AnalyticsService,
                    @Assisted private val prefix: String,
                    @Assisted private val id: Int,
                  ) {
                    interface Factory {
                      fun create(prefix: String, id: Int): ListItemFormatter
                    }
                  }

                  // 2. Inject the Factory with the DSL
                  val factory = inject<ListItemFormatter.Factory>()
                  val formatter = factory.create("Item", 123)
                """.trimIndent()
      )
      Spacer(Modifier.height(16.dp))
      Text("Live Demo:", style = MaterialTheme.typography.titleMedium)
      Spacer(Modifier.height(8.dp))
      Text(state.formattedText)
    }
  }
}
