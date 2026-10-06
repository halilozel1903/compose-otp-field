package io.github.halilozel1903.otp.sample

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement

/** A screenshot scene, or the whole sign-in flow when [scene] is `null`. */
@Composable
fun SampleApp(scene: Scene?) {
    when (scene) {
        Scene.Otp -> VerifyScreen(initialCode = "4829", initialFailures = 0, screenshot = true, onVerified = {})
        Scene.Error -> VerifyScreen(initialCode = "482193", initialFailures = 1, screenshot = true, onVerified = {})
        Scene.Pin -> PinScreen(initialPin = "27", screenshot = true, onUnlocked = {})
        Scene.Tablet -> TransferVerifyScreen(initialCode = "6105", screenshot = true, onVerified = {})
        null -> DemoFlow()
    }
}

private enum class Step { Code, Pin, Done }

@Composable
private fun DemoFlow() {
    var step by rememberSaveable { mutableStateOf(Step.Code) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 720.dp && maxHeight >= 640.dp
        when (step) {
            Step.Code -> if (wide) {
                TransferVerifyScreen(initialCode = "", screenshot = false, onVerified = { step = Step.Pin })
            } else {
                VerifyScreen(initialCode = "", initialFailures = 0, screenshot = false, onVerified = { step = Step.Pin })
            }
            Step.Pin -> PinScreen(initialPin = "", screenshot = false, onUnlocked = { step = Step.Done })
            Step.Done -> DoneScreen(onRestart = { step = Step.Code })
        }
    }
}

@Composable
private fun DoneScreen(onRestart: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ShieldIllustration(Modifier.size(180.dp))
        Spacer(Modifier.height(24.dp))
        Text("You're all set", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Your code and PIN were accepted.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRestart) { Text("Start over") }
    }
}
