package io.github.halilozel1903.otp

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.github.halilozel1903.otp.core.OtpCodeExtractor
import io.github.halilozel1903.otp.core.OtpFormat

/** The state of [rememberSmsRetriever]. */
@Stable
public class SmsRetrieverState internal constructor(
    /** Whether Google Play services' SMS User Consent API is on the classpath. */
    public val isAvailable: Boolean,
) {
    internal var session by mutableIntStateOf(0)

    /** Whether it is waiting for an SMS right now. Listening stops after a message or after five minutes. */
    public var isListening: Boolean by mutableStateOf(false)
        internal set

    /** The full text of the last SMS the user agreed to share, or `null`. */
    public var lastMessage: String? by mutableStateOf(null)
        internal set

    /** Listens for the next SMS again, for example after "Resend code". */
    public fun restart() {
        session++
    }
}

/**
 * Reads the one-time code from the next SMS with the
 * [SMS User Consent API](https://developers.google.com/identity/sms-retriever/user-consent/overview): when an SMS
 * with a code arrives, Android asks the user once whether the app may read that message, and [onCode] gets the code.
 * No SMS permission is needed.
 *
 * This is optional. It works when the app depends on Google Play services' SMS API itself:
 *
 * ```
 * implementation("com.google.android.gms:play-services-auth-api-phone:18.1.0")
 * ```
 *
 * Without that dependency, or on devices without Google Play services, it does nothing and
 * [SmsRetrieverState.isAvailable] is `false`. Codes still arrive through the keyboard's SMS suggestion and
 * autofill, which [OtpField] supports out of the box.
 *
 * ```
 * var code by rememberSaveable { mutableStateOf("") }
 * val sms = rememberSmsRetriever(onCode = { code = it })
 * OtpField(value = code, onValueChange = { code = it })
 * ResendCodeButton(timer, onResend = { api.sendCode(); sms.restart() })
 * ```
 *
 * @param onCode called with the code found in the message.
 * @param length the code length the default [extractor] looks for.
 * @param senderPhoneNumber only messages from this number, or `null` for any sender that is not in the contacts.
 * @param extractor how to find the code in the message.
 * @param enabled set to `false` to stop listening.
 */
@Composable
public fun rememberSmsRetriever(
    onCode: (String) -> Unit,
    length: Int = 6,
    senderPhoneNumber: String? = null,
    extractor: OtpCodeExtractor = remember(length) { OtpCodeExtractor(OtpFormat(length)) },
    enabled: Boolean = true,
): SmsRetrieverState {
    val context = LocalContext.current
    val state = remember { SmsRetrieverState(SmsUserConsent.isAvailable()) }
    val currentOnCode by rememberUpdatedState(onCode)
    val currentExtractor by rememberUpdatedState(extractor)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val message = result.data?.getStringExtra(SmsUserConsent.EXTRA_SMS_MESSAGE)
            state.lastMessage = message
            currentExtractor.extract(message)?.let { currentOnCode(it) }
        }
    }

    DisposableEffect(context, state.isAvailable, enabled, senderPhoneNumber, state.session) {
        if (!state.isAvailable || !enabled) {
            state.isListening = false
            return@DisposableEffect onDispose { }
        }
        val registration = SmsUserConsent.listen(
            context = context,
            senderPhoneNumber = senderPhoneNumber,
            onConsentIntent = { intent ->
                state.isListening = false
                runCatching { launcher.launch(intent) }
            },
            onTimeout = { state.isListening = false },
        )
        state.isListening = registration != null
        onDispose {
            registration?.let { runCatching { context.unregisterReceiver(it) } }
            state.isListening = false
        }
    }
    return state
}
