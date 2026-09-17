package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.jarvis.ui.JarvisScreen
import com.example.jarvis.ui.JarvisViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaceBlack

class MainActivity : ComponentActivity() {

  private val jarvisViewModel: JarvisViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = SpaceBlack
        ) {
          JarvisScreen(viewModel = jarvisViewModel)
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    jarvisViewModel.refreshBatteryTelemetry()
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "JARVIS AI Online: $name", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Commander") }
}

