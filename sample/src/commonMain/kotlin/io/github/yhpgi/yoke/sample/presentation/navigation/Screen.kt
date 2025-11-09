package io.github.yhpgi.yoke.sample.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * A sealed interface representing all the screens in the application for type-safe navigation.
 */
@Serializable
sealed interface Screen {
  /** The home screen displaying the list of features. */
  @Serializable
  data object Home : Screen

  /** The screen demonstrating basic @Injectable usage. */
  @Serializable
  data object BasicInject : Screen

  /** The screen demonstrating interface binding with @Binds. */
  @Serializable
  data object Binds : Screen

  /** The screen demonstrating dependency providing with @Provides. */
  @Serializable
  data object Provides : Screen

  /** The screen demonstrating scopes and subcomponents. */
  @Serializable
  data object Scopes : Screen

  /** The screen demonstrating dependency qualifiers. */
  @Serializable
  data object Qualifiers : Screen

  /** The screen demonstrating assisted injection. */
  @Serializable
  data object AssistedInject : Screen

  /** The screen demonstrating non-composable injection. */
  @Serializable
  data object Worker : Screen

  /** The screen demonstrating the full DSL API. */
  @Serializable
  data object Dsl : Screen
}
