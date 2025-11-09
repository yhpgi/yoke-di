package io.github.yhpgi.yoke.sample.presentation.feature.worker

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.yhpgi.yoke.di.injectViewModel
import io.github.yhpgi.yoke.sample.presentation.shared.InfoCard

/**
 * A screen that demonstrates non-composable dependency injection.
 */
@Composable
fun WorkerScreen() {
  val viewModel = injectViewModel<WorkerViewModel>()
  val state by viewModel.state.collectAsState()

  Scaffold { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
      InfoCard(
        title = "Non-Composable Injection",
        description = "Use injectGlobal() to inject dependencies outside of @Composable functions. Perfect for background workers, services, or any non-UI code.",
        codeSnippet = """
     // In a Worker or background task
     class SampleWorker {
       fun doWork(): String {
         // Global injection with DSL
         val analytics = injectGlobal<AnalyticsService>()
         return analytics.trackEvent("WorkerExecuted")
       }
     }

     // With a qualifier
     val repo = injectGlobal<Repository> {
       qualifiedBy(PremiumUser::class)
     }

     // In Android, you can also use a context extension
     class MyActivity : Activity() {
       private val repo by lazy {
         applicationContext.inject<Repository>()
       }
     }
    """.trimIndent()
      )
      Spacer(Modifier.height(16.dp))
      Text("Live Demo:", style = MaterialTheme.typography.titleMedium)
      Spacer(Modifier.height(8.dp))

      Button(onClick = { viewModel.onEvent(WorkerEvent.ExecuteWorker) }) {
        Text("Execute Worker")
      }

      Spacer(Modifier.height(8.dp))
      Text(state.workerResult)

      Spacer(Modifier.height(16.dp))
      Text("Platform: ${state.platformName}", style = MaterialTheme.typography.bodyMedium)
    }
  }
}
