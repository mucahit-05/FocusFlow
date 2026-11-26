package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.repository.FocusRepository
import com.example.ui.PomodoroViewModel
import com.example.ui.PomodoroViewModelFactory
import com.example.ui.screens.AppIntroSplashScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.FocusFlowTheme
import com.example.util.AmbientAudioEngine
import com.example.util.SoundAndVibrationHelper

class MainActivity : ComponentActivity() {

    companion object {
        // Tracks whether splash has already been shown during this process lifetime
        // so returning from background never triggers the splash
        private var hasShownIntroSplash = false
    }

    private val viewModel: PomodoroViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        val repository = FocusRepository(database.taskDao(), database.sessionDao())
        val alertHelper = SoundAndVibrationHelper(applicationContext)
        val audioEngine = AmbientAudioEngine(applicationContext)
        val preferences = com.example.util.SettingsPreferences(applicationContext)
        PomodoroViewModelFactory(repository, alertHelper, audioEngine, preferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep screen on during focus sessions for continuous countdown visibility
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val shouldShowSplash = !hasShownIntroSplash && savedInstanceState == null

        setContent {
            val timerState by viewModel.timerState.collectAsStateWithLifecycle()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val appStrings by viewModel.appStrings.collectAsStateWithLifecycle()
            var isShowingSplash by remember { mutableStateOf(shouldShowSplash) }

            FocusFlowTheme(
                mode = timerState.currentMode,
                themeMode = com.example.ui.theme.AppThemeMode.fromCode(settings.themeMode)
            ) {
                Crossfade(targetState = isShowingSplash, animationSpec = tween(400), label = "splash_fade") { showSplash ->
                    if (showSplash) {
                        AppIntroSplashScreen(
                            strings = appStrings,
                            onAnimationFinished = {
                                hasShownIntroSplash = true
                                isShowingSplash = false
                            }
                        )
                    } else {
                        MainScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
