package io.github.yhpgi.yoke.sample

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.yhpgi.yoke.di.YokeApplication
import io.github.yhpgi.yoke.sample.presentation.feature.assisted.AssistedInjectScreen
import io.github.yhpgi.yoke.sample.presentation.feature.basic.BasicInjectScreen
import io.github.yhpgi.yoke.sample.presentation.feature.binds.BindsScreen
import io.github.yhpgi.yoke.sample.presentation.feature.dsl.DslScreen
import io.github.yhpgi.yoke.sample.presentation.feature.home.HomeScreen
import io.github.yhpgi.yoke.sample.presentation.feature.provides.ProvidesScreen
import io.github.yhpgi.yoke.sample.presentation.feature.qualifier.QualifierScreen
import io.github.yhpgi.yoke.sample.presentation.feature.scope.ScopeScreen
import io.github.yhpgi.yoke.sample.presentation.feature.worker.WorkerScreen
import io.github.yhpgi.yoke.sample.presentation.navigation.Screen

/**
 * The Composable root of the sample application.
 * This function initializes Yoke DI, the Material theme, and navigation.
 */
@Composable
fun App() {
  YokeApplication {
    MaterialTheme {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = Screen.Home, enterTransition = {
        slideIntoContainer(
          towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow
          )
        )
      }, exitTransition = {
        slideOutOfContainer(
          towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow
          ), targetOffset = { fullOffset -> (fullOffset * 0.3f).toInt() })
      }, popEnterTransition = {
        slideIntoContainer(
          towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow
          ), initialOffset = { fullOffset -> (fullOffset * 0.3f).toInt() })
      }, popExitTransition = {
        slideOutOfContainer(
          towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow
          )
        )
      }) {
        composable<Screen.Home> {
          HomeScreen(onNavigate = { screen -> navController.navigate(screen) })
        }
        composable<Screen.BasicInject> { BasicInjectScreen() }
        composable<Screen.Binds> { BindsScreen() }
        composable<Screen.Provides> { ProvidesScreen() }
        composable<Screen.Scopes> { ScopeScreen() }
        composable<Screen.Qualifiers> { QualifierScreen() }
        composable<Screen.AssistedInject> { AssistedInjectScreen() }
        composable<Screen.Worker> { WorkerScreen() }
        composable<Screen.Dsl> { DslScreen() }
      }
    }
  }
}
