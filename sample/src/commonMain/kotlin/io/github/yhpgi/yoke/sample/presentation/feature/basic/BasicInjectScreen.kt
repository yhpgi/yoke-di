package io.github.yhpgi.yoke.sample.presentation.feature.basic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
 * A screen that demonstrates the basic injection feature using `@Injectable`.
 * This is the most fundamental feature of Yoke DI.
 */
@Composable
fun BasicInjectScreen() {
  val viewModel = injectViewModel<BasicInjectViewModel>()
  val state by viewModel.state.collectAsState()

  Scaffold { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
      InfoCard(
        title = "Basic Injection (@Injectable)",
        description = "The simplest feature. Mark a class with @Injectable and @ContributesTo a component, and Yoke will know how to create it. Any dependencies in its constructor will be resolved automatically.",
        codeSnippet = """
                  // 1. Annotate the class
                  @Injectable
                  @ContributesTo(AppComponent::class)
                  class Greeter { ... }

                  // 2. Inject with the DSL
                  val greeter = inject<Greeter>()

                  // 3. Inject a ViewModel
                  val viewModel = injectViewModel<MyViewModel>()

                  // or with options
                  val viewModelWithOptions = injectViewModel<MyViewModel> {
                    qualifiedBy(SomeQualifier::class)
                    scopedTo(customOwner)
                  }
                """.trimIndent()
      )
      Spacer(Modifier.height(16.dp))
      Text("Live Demo:")
      Spacer(Modifier.height(8.dp))
      Text(state.greeting)
    }
  }
}
