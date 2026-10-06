package io.github.halilozel1903.otp.sample

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.otp.OtpField
import io.github.halilozel1903.otp.OtpFieldDefaults
import io.github.halilozel1903.otp.OtpFieldStyle
import io.github.halilozel1903.otp.ResendCodeButton
import io.github.halilozel1903.otp.rememberResendTimer
import io.github.halilozel1903.otp.rememberSmsRetriever

/** The four built-in styles, for the style picker. */
@Composable
fun sampleStyles(): List<Pair<String, OtpFieldStyle>> = listOf<Pair<String, OtpFieldStyle>>(
    "Boxed" to OtpFieldDefaults.boxed(),
    "Rounded" to OtpFieldDefaults.rounded(),
    "Underline" to OtpFieldDefaults.underline(),
    "Circle" to OtpFieldDefaults.circle(),
)

/**
 * "Verify it's you" on a phone: a 6-digit code sent by SMS, with SMS User Consent, a resend countdown and
 * attempt limiting.
 *
 * @param screenshot a still screen for README screenshots: no keyboard, no SMS listener, a steady cursor.
 */
@Composable
fun VerifyScreen(
    initialCode: String,
    initialFailures: Int,
    screenshot: Boolean,
    onVerified: () -> Unit,
) {
    val verifier = remember { CodeVerifier(initialFailures) }
    var code by rememberSaveable { mutableStateOf(initialCode) }
    var styleIndex by rememberSaveable { mutableIntStateOf(0) }
    val styles = sampleStyles()
    val timer = rememberResendTimer(durationSeconds = 60, maxResends = 3)
    val submit: (String) -> Unit = { candidate -> if (verifier.verify(candidate)) onVerified() }
    val sms = rememberSmsRetriever(
        onCode = { received ->
            code = received
            submit(received)
        },
        enabled = !screenshot,
    )

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {}) { BackArrow(MaterialTheme.colorScheme.onSurface) }
            Spacer(Modifier.weight(1f))
            LumenLogo(Modifier.padding(end = 16.dp))
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))
            PhoneCodeIllustration(Modifier.size(156.dp))
            Spacer(Modifier.height(24.dp))
            Text(
                "Verify it's you",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Enter the 6-digit code we sent to $MASKED_PHONE",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            OtpField(
                value = code,
                onValueChange = {
                    code = it
                    if (verifier.isError) verifier.clearError()
                },
                length = 6,
                style = styles[styleIndex].second,
                modifier = Modifier.widthIn(max = 420.dp),
                isError = verifier.isError,
                autoFocus = !screenshot,
                onComplete = submit,
                blinkCursor = !screenshot,
                showCursorWithoutFocus = screenshot,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                verifier.message ?: "Codes from SMS are filled in automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (verifier.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            ResendCodeButton(
                state = timer,
                onResend = {
                    code = ""
                    verifier.clearError()
                    sms.restart()
                },
            )
            if (!screenshot) {
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    styles.forEachIndexed { index, (name, _) ->
                        FilterChip(
                            selected = index == styleIndex,
                            onClick = { styleIndex = index },
                            label = { Text(name) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = { submit(code) },
                enabled = code.length == 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text("Verify")
            }
            if (!screenshot) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Demo code: $DEMO_CODE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
