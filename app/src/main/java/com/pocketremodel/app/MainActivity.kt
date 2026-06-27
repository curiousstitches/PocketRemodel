package com.pocketremodel.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.pocketremodel.app.data.SettingsStore
import com.pocketremodel.app.ui.ArRemodelScreen
import com.pocketremodel.app.ui.SetupScreen
import com.pocketremodel.app.ui.SplashScreen
import com.pocketremodel.app.ui.theme.PocketRemodelTheme
import kotlinx.coroutines.flow.first

/** The three places the app can be. */
private enum class Screen { SPLASH, SETUP, REMODEL }

class MainActivity : ComponentActivity() {

    private val arVm: ArViewModel by viewModels()
    private val setupVm: SetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PocketRemodelTheme {
                AppRoot(arVm, setupVm) { hasPermissions() }
            }
        }
    }

    @Composable
    private fun AppRoot(
        arVm: ArViewModel,
        setupVm: SetupViewModel,
        hasPermissions: () -> Boolean
    ) {
        var screen by remember { mutableStateOf(Screen.SPLASH) }
        var permsGranted by remember { mutableStateOf(hasPermissions()) }
        val settings = remember { SettingsStore(this) }

        val permLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result -> permsGranted = result.values.all { it } }

        when (screen) {

            Screen.SPLASH -> SplashScreen(onFinished = {
                // Decide where to go once the splash dissolves.
                screen = Screen.SETUP   // SetupScreen self-skips if a key already works
            })

            Screen.SETUP -> {
                // Returning user who already verified a key skips setup entirely.
                LaunchedEffect(Unit) {
                    if (settings.isConfigured() && settings.verifiedFlow.first()) {
                        screen = Screen.REMODEL
                    }
                }
                SetupScreen(setupVm, onReady = { screen = Screen.REMODEL })
            }

            Screen.REMODEL -> {
                LaunchedEffect(Unit) { if (!permsGranted) permLauncher.launch(REQUIRED) }
                if (permsGranted) {
                    ArRemodelScreen(arVm)
                } else {
                    Box(
                        Modifier.fillMaxSize().background(Color(0xFF0E1116)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Pocket Remodel needs the camera and microphone to redesign your room.",
                            color = Color.White, modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            }
        }
    }

    private fun hasPermissions(): Boolean = REQUIRED.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    }
}
