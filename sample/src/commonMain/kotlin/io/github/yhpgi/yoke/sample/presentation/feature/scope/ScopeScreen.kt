package io.github.yhpgi.yoke.sample.presentation.feature.scope

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
import io.github.yhpgi.yoke.di.Scope
import io.github.yhpgi.yoke.di.injectViewModel
import io.github.yhpgi.yoke.sample.di.UserComponent
import io.github.yhpgi.yoke.sample.presentation.shared.InfoCard

/**
 * A screen that demonstrates the use of Scopes with the DSL.
 */
@Composable
fun ScopeScreen() {
  val viewModel = injectViewModel<ScopeViewModel>()
  val state by viewModel.state.collectAsState()

  Scaffold { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
      InfoCard(
        title = "Scoping & ViewModels",
        description = "Scopes bind the lifecycle of a dependency to a component. Use the DSL for cleaner scope management.",
        codeSnippet = """
                  // 1. ViewModel injection with DSL
                  val viewModel = injectViewModel<MyViewModel>()

                  // 2. With options
                  val viewModelWithOptions = injectViewModel<MyViewModel> {
                    qualifiedBy(SomeQualifier::class)
                    scopedTo(customOwner)
                  }

                  // 3. Scope management
                  scope<UserComponent> {
                    active { isLoggedIn }

                    whenActive {
                      val vm = injectViewModel<UserViewModel>()
                      Text("Hello!")
                    }

                    whenInactive {
                      Text("Please log in")
                    }
                  }
                """.trimIndent()
      )
      Spacer(Modifier.height(16.dp))
      Text("Live Demo:", style = MaterialTheme.typography.titleMedium)
      Spacer(Modifier.height(8.dp))

      Text(
        if (state.isLoggedIn) "Status: Logged In" else "Status: Logged Out",
        color = if (state.isLoggedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
      )
      Spacer(Modifier.height(8.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Button(
          onClick = { viewModel.onEvent(ScopeEvent.Login) },
          enabled = !state.isLoggedIn
        ) { Text("Login") }
        Button(
          onClick = { viewModel.onEvent(ScopeEvent.Logout) },
          enabled = state.isLoggedIn
        ) { Text("Logout") }
      }
      Spacer(Modifier.height(16.dp))

      Text("Singleton Scope:", style = MaterialTheme.typography.titleSmall)
      Text(state.analyticsMessage)
      Text("Object hash codes will be the same as on other screens.", style = MaterialTheme.typography.bodySmall)

      Spacer(Modifier.height(16.dp))

      Text("User Scope:", style = MaterialTheme.typography.titleSmall)
      Scope<UserComponent> {
        active { state.isLoggedIn }

        whenActive {
          val userViewModel = injectViewModel<UserScopeViewModel>()
          val userState by userViewModel.state.collectAsState()
          Text("Hello, ${userState.username}!")
          Text("This object only exists while you are logged in.", style = MaterialTheme.typography.bodySmall)
        }

        whenInactive {
          Text("Login to create the UserComponent and its dependencies.")
        }
      }
    }
  }
}
