package io.github.yhpgi.yoke.sample.presentation.feature.dsl

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
import io.github.yhpgi.yoke.di.inject
import io.github.yhpgi.yoke.sample.di.PremiumUser
import io.github.yhpgi.yoke.sample.di.UserComponent
import io.github.yhpgi.yoke.sample.domain.AuthRepository
import io.github.yhpgi.yoke.sample.domain.UserRepository
import io.github.yhpgi.yoke.sample.presentation.shared.InfoCard
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * A screen that demonstrates the new DSL-style API for the entire Yoke library.
 */
@Composable
fun DslScreen() {
  val authRepository = inject<AuthRepository>()
  val isLoggedIn by authRepository.isLoggedIn.collectAsState()

  Scaffold { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
    ) {
      InfoCard(
        title = "Complete DSL-Style API",
        description = "Yoke now provides a DSL for all its operations. This new API is more expressive, type-safe, and reduces boilerplate.",
        codeSnippet = """
          // 1. Dependency Injection
          val repo = inject<Repository>()
          val qualifiedRepo = inject<Repository> {
            qualifiedBy(PremiumUser::class)
          }

          // 2. ViewModel Injection
          val viewModel = injectViewModel<MyViewModel>()
          val scopedViewModel = injectViewModel<MyViewModel> {
            qualifiedBy(SomeQualifier::class)
            scopedTo(customOwner)
          }

          // 3. Scope Management
          scope<UserComponent> {
            active { isLoggedIn }
            whenActive { Text("User is logged in") }
            whenInactive { Text("Please log in") }
            onActivate { println("Session started") }
            onDeactivate { println("Session ended") }
          }

          // 4. Global Injection (non-Composable)
          val service = injectGlobal<AnalyticsService>()
          val qualifiedService = injectGlobal<Service> {
            qualifiedBy(Premium::class)
          }

          // 5. Android Context Extension
          // val contextRepo = context.inject<Repository>()
        """.trimIndent()
      )

      Spacer(Modifier.height(16.dp))
      Text("Live Demo:", style = MaterialTheme.typography.titleMedium)
      Spacer(Modifier.height(8.dp))

      Text(
        if (isLoggedIn) "Status: Logged In" else "Status: Logged Out",
        color = if (isLoggedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
      )
      Spacer(Modifier.height(8.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Button(
          onClick = {
            MainScope().launch {
              authRepository.login()
            }
          }, enabled = !isLoggedIn
        ) { Text("Login") }
        Spacer(Modifier.width(16.dp))
        Button(
          onClick = {
            MainScope().launch {
              authRepository.logout()
            }
          }, enabled = isLoggedIn
        ) { Text("Logout") }
      }

      Spacer(Modifier.height(16.dp))
      Text("DSL Injection Examples:", style = MaterialTheme.typography.titleSmall)
      Spacer(Modifier.height(8.dp))

      Text("✓ Basic injection: inject<Repository>()")
      Text("✓ Qualified injection: inject<Repo> { qualifiedBy(Premium::class) }")
      Text("✓ ViewModel injection: injectViewModel<MyVM>()")
      Text("✓ Global injection: injectGlobal<Service>()")

      Spacer(Modifier.height(16.dp))
      Text("DSL Scope Management:", style = MaterialTheme.typography.titleSmall)
      Spacer(Modifier.height(8.dp))

      Scope<UserComponent> {
        active { isLoggedIn }

        whenActive {
          val userRepository = inject<UserRepository> {
            qualifiedBy(PremiumUser::class)
          }
          Text("✓ UserComponent is active (DSL)")
          Text("✓ User: ${userRepository.getUsername()}")
          Text("✓ Managed automatically with scope { } DSL")
        }

        whenInactive {
          Text("○ UserComponent is inactive")
          Text("○ Log in to activate the scope")
        }

        onActivate {
          println("🎉 User session activated via scope { } DSL!")
        }

        onDeactivate {
          println("👋 User session deactivated via scope { } DSL!")
        }
      }

      Spacer(Modifier.height(16.dp))
      Text("Benefits:", style = MaterialTheme.typography.titleSmall)
      Text("• Cleaner, more readable code")
      Text("• Type-safe API with compile-time checks")
      Text("• Less boilerplate")
      Text("• Automatic lifecycle management")
      Text("• Consistent API across all features")
    }
  }
}
