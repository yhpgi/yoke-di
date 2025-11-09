package io.github.yhpgi.yoke.sample.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.yhpgi.yoke.di.Scope
import io.github.yhpgi.yoke.di.inject
import io.github.yhpgi.yoke.sample.di.UserComponent
import io.github.yhpgi.yoke.sample.domain.AuthRepository

/**
 * A Composable that manages the lifecycle of the `UserComponent` using the DSL.
 * It is now much simpler with the `scope { }` DSL.
 *
 * @param loggedInContent The content to display when the user is logged in.
 * @param loggedOutContent The content to display when the user is logged out.
 */
@Composable
fun UserSession(
  loggedInContent: @Composable () -> Unit,
  loggedOutContent: @Composable () -> Unit = {}
) {
  val authRepository = inject<AuthRepository>()
  val isLoggedIn by authRepository.isLoggedIn.collectAsState()

  Scope<UserComponent> {
    active { isLoggedIn }
    whenActive(loggedInContent)
    whenInactive(loggedOutContent)
  }
}
