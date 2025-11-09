package io.github.yhpgi.yoke.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/**
 * The main entry point for the sample application on the Android platform.
 */
class MainActivity : ComponentActivity() {
  /**
   * Called when the activity is first created.
   * This method sets up the UI content using Jetpack Compose.
   */
  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      App()
    }
  }
}
