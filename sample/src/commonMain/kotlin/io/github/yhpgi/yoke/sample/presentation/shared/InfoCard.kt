package io.github.yhpgi.yoke.sample.presentation.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * A reusable Composable for displaying an information card
 * about a DI feature.
 *
 * @param title The title of the feature.
 * @param description A brief description of the feature.
 * @param codeSnippet An example code snippet for the feature.
 */
@Composable
fun InfoCard(
  title: String,
  description: String,
  codeSnippet: String,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(title, style = MaterialTheme.typography.titleLarge)
      Spacer(Modifier.height(8.dp))
      Text(description, style = MaterialTheme.typography.bodyMedium)
      Spacer(Modifier.height(16.dp))
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = codeSnippet.trimIndent(),
          style = MaterialTheme.typography.bodySmall,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(12.dp)
        )
      }
    }
  }
}
