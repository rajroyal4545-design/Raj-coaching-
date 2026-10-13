package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.CoachingApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CoachingViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: CoachingViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val themeMode by viewModel.themeMode.collectAsState()
      val themePalette by viewModel.themePalette.collectAsState()
      MyApplicationTheme(themeMode = themeMode, themePalette = themePalette) {
        Surface(modifier = Modifier.fillMaxSize()) {
          CoachingApp(viewModel = viewModel)
        }
      }
    }
  }
}
