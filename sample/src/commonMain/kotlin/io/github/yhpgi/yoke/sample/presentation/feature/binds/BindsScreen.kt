package io.github.yhpgi.yoke.sample.presentation.feature.binds

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
 * A screen that demonstrates the interface binding feature (@Binds).
 */
@Composable
fun BindsScreen() {
  val viewModel = injectViewModel<BindsViewModel>()
  val state by viewModel.state.collectAsState()

  Scaffold { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
      InfoCard(
        title = "Interface Binding (@Binds)",
        description = "Promotes clean architecture by allowing you to inject an interface and letting Yoke provide the concrete implementation.",
        codeSnippet = """
                  // 1. Define the interface
                  interface MessageRepository { ... }

                  // 2. Annotate the implementation
                  @Injectable
                  @Binds(to = MessageRepository::class)
                  @ContributesTo(AppComponent::class)
                  class MessageRepositoryImpl : MessageRepository

                  // 3. Inject the interface with the DSL
                  val repository = inject<MessageRepository>()
                """.trimIndent()
      )
      Spacer(Modifier.height(16.dp))
      Text("Live Demo:", style = MaterialTheme.typography.titleMedium)
      Spacer(Modifier.height(8.dp))
      Text(state.message)
    }
  }
}
