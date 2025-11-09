package io.github.yhpgi.yoke.sample.presentation.feature.qualifier

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
import io.github.yhpgi.yoke.di.Scope
import io.github.yhpgi.yoke.di.inject
import io.github.yhpgi.yoke.di.injectViewModel
import io.github.yhpgi.yoke.sample.di.UserComponent
import io.github.yhpgi.yoke.sample.domain.AuthRepository
import io.github.yhpgi.yoke.sample.presentation.shared.InfoCard

/**
 * A screen that demonstrates the use of Qualifiers with the DSL.
 */
@Composable
fun QualifierScreen() {
  val viewModel = injectViewModel<QualifierViewModel>()
  val state by viewModel.state.collectAsState()
  val authRepository = inject<AuthRepository>()
  val isLoggedIn by authRepository.isLoggedIn.collectAsState()

  Scaffold { padding ->
    Scope<UserComponent> {
      active { isLoggedIn }

      whenActive {
        LaunchedEffect(Unit) { viewModel.onEvent(QualifierEvent.LoadUsernames) }

        Column(
          modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
            .verticalScroll(rememberScrollState())
        ) {
          InfoCard(
            title = "Qualifiers (@QualifiedBy)",
            description = "Used when you need to provide multiple different implementations of the same interface. Use the DSL for qualified injection.",
            codeSnippet = """
                      // 1. Create qualifier annotations
                      @YokeQualifier annotation class GuestUser
                      @YokeQualifier annotation class PremiumUser

                      // 2. Apply to implementations
                      @Injectable @QualifiedBy(GuestUser::class)
                      class GuestRepoImpl : UserRepository

                      // 3. Inject with the DSL
                      val repo = inject<UserRepository> {
                        qualifiedBy(GuestUser::class)
                      }

                      // or in a ViewModel constructor
                      @Injectable
                      class MyViewModel(
                        @QualifiedBy(GuestUser::class)
                        private val repo: UserRepository
                      ) : ViewModel()
                    """.trimIndent()
          )
          Spacer(Modifier.height(16.dp))
          Text("Live Demo:", style = MaterialTheme.typography.titleMedium)
          Spacer(Modifier.height(8.dp))
          Text("Guest User: ${state.guestUsername}")
          Text("Premium User: ${state.premiumUsername}")
        }
      }

      whenInactive {
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
          Text("Please log in to see this feature.", style = MaterialTheme.typography.bodyLarge)
          Text(
            "This screen requires an active user session to create the UserComponent.",
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }
    }
  }
}
