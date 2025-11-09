package io.github.yhpgi.yoke.sample.presentation.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.yhpgi.yoke.di.injectViewModel
import io.github.yhpgi.yoke.sample.presentation.navigation.Screen

/**
 * The main screen of the application, displaying a list of Yoke DI features.
 * @param onNavigate A callback to navigate to a selected feature screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
  val viewModel = injectViewModel<HomeViewModel>()
  val state by viewModel.state.collectAsState()

  Scaffold(
    topBar = { TopAppBar(title = { Text("Yoke DI Features") }) }
  ) { padding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(padding)
    ) {
      items(state.features) { feature ->
        Column {
          ListItem(
            headlineContent = { Text(feature.title) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
            modifier = Modifier.fillMaxWidth().clickable { onNavigate(feature.screen) }
          )
          HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
      }
    }
  }
}
