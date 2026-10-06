package io.github.halilozel1903.otp.sample

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.halilozel1903.otp.ElapsedRealtimeClock
import io.github.halilozel1903.otp.core.OtpAttemptLimiter
import io.github.halilozel1903.otp.core.OtpAttemptResult
import io.github.halilozel1903.otp.core.OtpTime

/**
 * Checks codes against the demo code and limits attempts like a bank would: three wrong codes lock entry for
 * 30 seconds, then 60, and so on. A real app checks the code on its server.
 */
@Stable
class CodeVerifier(initialFailures: Int = 0) {
    private val limiter = OtpAttemptLimiter(maxAttempts = 3, lockoutMillis = 30_000, clock = ElapsedRealtimeClock)

    var isError by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    init {
        repeat(initialFailures) { fail() }
    }

    /** Returns `true` for the right code. */
    fun verify(code: String): Boolean {
        if (!limiter.canAttempt()) {
            isError = true
            message = "Too many wrong codes. Try again in ${OtpTime.format(limiter.lockoutRemainingMillis())}."
            return false
        }
        if (code == DEMO_CODE) {
            limiter.recordSuccess()
            isError = false
            message = null
            return true
        }
        fail()
        return false
    }

    fun clearError() {
        isError = false
        message = null
    }

    private fun fail() {
        isError = true
        message = when (val result = limiter.recordFailure()) {
            is OtpAttemptResult.Retry -> {
                val left = result.remainingAttempts
                "That code doesn't match. $left ${if (left == 1) "attempt" else "attempts"} left."
            }
            is OtpAttemptResult.LockedOut ->
                "Too many wrong codes. Try again in ${OtpTime.format(result.durationMillis)}."
        }
    }
}
