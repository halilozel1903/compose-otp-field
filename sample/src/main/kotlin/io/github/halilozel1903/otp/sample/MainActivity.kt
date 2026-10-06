package io.github.halilozel1903.otp.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

/**
 * Lumen Bank, a fictional bank app that verifies sign-ins and transfers with compose-otp-field.
 * `scripts/screenshots.sh` starts it with `--es scene <scene>` to open a fixed screen for README screenshots:
 *
 * - `otp`: the 6-digit code screen, partially filled, with the cursor in the next cell
 * - `error`: the same screen after a wrong code (red cells, attempts left)
 * - `pin`: the 4-digit PIN screen with the keypad
 * - `tablet`: the transfer confirmation card with an illustration (tablets in landscape)
 *
 * Without a scene the app runs the whole flow: code (demo code 135790), then PIN (demo PIN 2580).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val scene = Scene.from(intent.getStringExtra(EXTRA_SCENE))
        setContent {
            BankTheme(dark = isSystemInDarkTheme()) {
                // The Surface makes text default to onBackground, so it stays readable in dark mode.
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                    SampleApp(scene = scene)
                }
            }
        }
    }

    companion object {
        const val EXTRA_SCENE = "scene"
    }
}
