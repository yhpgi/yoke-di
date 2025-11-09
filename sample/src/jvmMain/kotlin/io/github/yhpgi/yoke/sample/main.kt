package io.github.yhpgi.yoke.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * The main entry point for the sample application on the Desktop (JVM) platform.
 */
fun main() = application {
  Window(
    onCloseRequest = ::exitApplication,
    title = "Yoke DI Showcase"
  ) {
    App()
  }
}
