package io.github.halilozel1903.otp.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.otp.OtpField
import io.github.halilozel1903.otp.OtpFieldDefaults
import io.github.halilozel1903.otp.PinPad
import kotlinx.coroutines.delay

private const val PIN_LENGTH = 4

/** Unlock with a 4-digit PIN entered on the in-app keypad; the digits show as dots. */
@Composable
fun PinScreen(initialPin: String, screenshot: Boolean, onUnlocked: () -> Unit) {
    var pin by rememberSaveable { mutableStateOf(initialPin) }
    var wrong by remember { mutableStateOf(false) }

    LaunchedEffect(pin) {
        if (pin.length == PIN_LENGTH) {
            delay(250)
            if (pin == DEMO_PIN) {
                onUnlocked()
            } else {
                wrong = true
                delay(700)
                pin = ""
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(20.dp))
        LumenLogo()
        Spacer(Modifier.weight(1f))
        Initials("AM", size = 64.dp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Welcome back, Alex",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text("Enter your PIN", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(24.dp))
        OtpField(
            value = pin,
            onValueChange = { pin = it },
            length = PIN_LENGTH,
            style = OtpFieldDefaults.circle().copy(cellWidth = 56.dp, cellHeight = 56.dp, spacing = 18.dp, dotSize = 16.dp),
            secure = true,
            isError = wrong,
            readOnly = true,
            blinkCursor = !screenshot,
            showCursorWithoutFocus = true,
            contentDescription = "PIN",
        )
        Spacer(Modifier.height(8.dp))
        if (wrong) {
            Text(
                "Wrong PIN. Try again.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        } else {
            TextButton(onClick = {}) { Text("Forgot PIN?") }
        }
        Spacer(Modifier.weight(1f))
        PinPad(
            onDigit = { digit ->
                if (pin.length < PIN_LENGTH) {
                    wrong = false
                    pin += digit
                }
            },
            onBackspace = { pin = pin.dropLast(1) },
            onBackspaceLongPress = { pin = "" },
            bottomStart = {
                IconButton(onClick = {}) { FingerprintIcon(MaterialTheme.colorScheme.primary) }
            },
        )
        Spacer(Modifier.height(if (screenshot) 24.dp else 16.dp))
        if (!screenshot) {
            Text(
                "Demo PIN: $DEMO_PIN",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
