package io.github.halilozel1903.otp.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.otp.OtpField
import io.github.halilozel1903.otp.OtpFieldDefaults
import io.github.halilozel1903.otp.ResendCodeButton
import io.github.halilozel1903.otp.rememberResendTimer
import io.github.halilozel1903.otp.rememberSmsRetriever

/**
 * Transfer confirmation for tablets: a centered card with an illustration on one side and the code entry on the
 * other, using the rounded style with the code split into two groups of three.
 */
@Composable
fun TransferVerifyScreen(initialCode: String, screenshot: Boolean, onVerified: () -> Unit) {
    val verifier = remember { CodeVerifier() }
    var code by rememberSaveable { mutableStateOf(initialCode) }
    val timer = rememberResendTimer(durationSeconds = 45, maxResends = 3)
    val submit: (String) -> Unit = { candidate -> if (verifier.verify(candidate)) onVerified() }
    val sms = rememberSmsRetriever(
        onCode = { received ->
            code = received
            submit(received)
        },
        enabled = !screenshot,
    )
    val scheme = MaterialTheme.colorScheme

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(scheme.primaryContainer.copy(alpha = 0.55f), scheme.background)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = scheme.surfaceContainerLowest,
            shadowElevation = 6.dp,
            modifier = Modifier
                .widthIn(max = 980.dp)
                .fillMaxWidth()
                .height(580.dp),
        ) {
            Row {
                Column(
                    Modifier
                        .weight(0.85f)
                        .fillMaxHeight()
                        .background(scheme.primaryContainer)
                        .padding(36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LumenLogo(Modifier.align(Alignment.Start))
                    Spacer(Modifier.weight(1f))
                    ShieldIllustration(Modifier.size(240.dp))
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Every transfer is protected",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "We ask for a one-time code before money leaves your account.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.weight(1f))
                }
                Column(
                    Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                        .padding(horizontal = 48.dp, vertical = 40.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Step 2 of 2", style = MaterialTheme.typography.labelLarge, color = scheme.primary)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Confirm your transfer",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(20.dp))
                    TransferSummary()
                    Spacer(Modifier.height(28.dp))
                    Text(
                        "Enter the 6-digit code we sent to $MASKED_PHONE",
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    OtpField(
                        value = code,
                        onValueChange = {
                            code = it
                            if (verifier.isError) verifier.clearError()
                        },
                        length = 6,
                        style = OtpFieldDefaults.rounded().copy(groupSize = 3, cellWidth = 54.dp, cellHeight = 62.dp),
                        isError = verifier.isError,
                        autoFocus = !screenshot,
                        onComplete = submit,
                        blinkCursor = !screenshot,
                        showCursorWithoutFocus = screenshot,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            verifier.message ?: if (screenshot) "Expires in 10 minutes" else "Demo code: $DEMO_CODE",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (verifier.isError) scheme.error else scheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        ResendCodeButton(
                            state = timer,
                            onResend = {
                                code = ""
                                verifier.clearError()
                                sms.restart()
                            },
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {}, modifier = Modifier.height(52.dp)) { Text("Cancel") }
                        Button(
                            onClick = { submit(code) },
                            enabled = code.length == 6,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                        ) {
                            Text("Confirm transfer")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransferSummary() {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .background(scheme.surfaceContainer, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Initials("AM")
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Alex Morgan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Lumen Everyday ···· 7710",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("$2,450.00", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Arrives today", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
    }
}
