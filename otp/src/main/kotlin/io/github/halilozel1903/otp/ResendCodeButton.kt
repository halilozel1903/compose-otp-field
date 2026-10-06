package io.github.halilozel1903.otp

import android.os.SystemClock
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.halilozel1903.otp.core.OtpClock
import io.github.halilozel1903.otp.core.OtpTime
import io.github.halilozel1903.otp.core.ResendCountdown
import kotlinx.coroutines.delay

/**
 * The state of a resend countdown, from [rememberResendTimer]. Reading [remainingSeconds], [canResend] or [label]
 * in composition updates the UI every second.
 */
@Stable
public class ResendTimerState internal constructor(private val countdown: ResendCountdown) {

    /** Whole seconds until a resend is allowed. */
    public var remainingSeconds: Int by mutableIntStateOf(countdown.remainingSeconds())
        private set

    /** How many times [resend] succeeded. */
    public var resendCount: Int by mutableIntStateOf(countdown.resendCount)
        private set

    private var exhausted by mutableStateOf(countdown.isExhausted())

    /** Resends still allowed. */
    public val resendsLeft: Int get() = (countdown.maxResends - resendCount).coerceAtLeast(0)

    /** Whether the countdown is over and a resend is still allowed. */
    public val canResend: Boolean get() = remainingSeconds == 0 && resendsLeft > 0

    /** Whether every resend has been used. */
    public val isExhausted: Boolean get() = exhausted

    /** The remaining time as `m:ss`, e.g. `0:42`. */
    public val label: String get() = OtpTime.format(remainingSeconds * 1000L)

    /** Records a resend and restarts the countdown; `false` when a resend is not allowed yet. */
    public fun resend(): Boolean {
        val done = countdown.resend()
        sync()
        return done
    }

    /** Restarts the countdown without counting a resend. */
    public fun restart() {
        countdown.restart()
        sync()
    }

    internal fun sync() {
        remainingSeconds = countdown.remainingSeconds()
        resendCount = countdown.resendCount
        exhausted = countdown.isExhausted()
    }

    internal val startedAtMillis: Long get() = countdown.startedAtMillis
}

/** A clock that keeps counting across process death and device sleep: `SystemClock.elapsedRealtime()`. */
public val ElapsedRealtimeClock: OtpClock = OtpClock { SystemClock.elapsedRealtime() }

/**
 * Remembers a resend countdown that starts now, survives configuration changes and process death, and ticks
 * every second while it is on screen.
 *
 * @param durationSeconds how long the user waits before a new code can be requested.
 * @param maxResends how many new codes may be requested in total.
 * @param clock the time source; the default uses `SystemClock.elapsedRealtime()`.
 */
@Composable
public fun rememberResendTimer(
    durationSeconds: Int = 60,
    maxResends: Int = Int.MAX_VALUE,
    clock: OtpClock = ElapsedRealtimeClock,
): ResendTimerState {
    val durationMillis = durationSeconds * 1000L
    val state = rememberSaveable(
        durationSeconds,
        maxResends,
        saver = resendTimerSaver(durationMillis, maxResends, clock),
    ) {
        ResendTimerState(ResendCountdown(durationMillis, maxResends, clock))
    }
    LaunchedEffect(state) {
        while (true) {
            state.sync()
            delay(TICK_MILLIS)
        }
    }
    return state
}

private const val TICK_MILLIS = 250L

private fun resendTimerSaver(
    durationMillis: Long,
    maxResends: Int,
    clock: OtpClock,
): Saver<ResendTimerState, Any> = listSaver<ResendTimerState, Long>(
    save = { listOf(it.startedAtMillis, it.resendCount.toLong()) },
    restore = { saved ->
        ResendTimerState(
            ResendCountdown(
                durationMillis = durationMillis,
                maxResends = maxResends,
                clock = clock,
                startedAtMillis = saved[0],
                resendCount = saved[1].toInt(),
            ),
        )
    },
)

/**
 * A text button that counts down ("Resend code in 0:42") and becomes "Resend code" when a new code may be
 * requested. Tapping it restarts [state] and calls [onResend].
 *
 * ```
 * val timer = rememberResendTimer(durationSeconds = 60, maxResends = 3)
 * ResendCodeButton(state = timer, onResend = { viewModel.sendCode() })
 * ```
 */
@Composable
public fun ResendCodeButton(
    state: ResendTimerState,
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Resend code",
    countdownText: (String) -> String = { "Resend code in $it" },
    exhaustedText: String = "No more codes can be sent",
) {
    TextButton(
        onClick = { if (state.resend()) onResend() },
        enabled = state.canResend,
        modifier = modifier,
    ) {
        Text(
            text = when {
                state.canResend -> text
                state.isExhausted -> exhaustedText
                else -> countdownText(state.label)
            },
        )
    }
}
