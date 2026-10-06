package io.github.halilozel1903.otp.core

/**
 * The "Resend code in 0:42" timer. It starts running when created; [resend] is allowed once it reaches zero and
 * restarts it, up to [maxResends] times.
 *
 * The time comes from [clock], so tests can move time forward. To survive process death, save
 * [startedAtMillis] and [resendCount] and pass them back with a clock that keeps counting across processes
 * (such as `SystemClock.elapsedRealtime` on Android).
 *
 * @param durationMillis how long to wait before a resend is allowed.
 * @param maxResends how many resends are allowed in total.
 */
public class ResendCountdown(
    public val durationMillis: Long,
    public val maxResends: Int = Int.MAX_VALUE,
    private val clock: OtpClock = OtpClock.Monotonic,
    startedAtMillis: Long = clock.nowMillis(),
    resendCount: Int = 0,
) {
    init {
        require(durationMillis >= 0) { "durationMillis must not be negative, was $durationMillis" }
        require(maxResends >= 0) { "maxResends must not be negative, was $maxResends" }
        require(resendCount >= 0) { "resendCount must not be negative, was $resendCount" }
    }

    /** When the current countdown started, in [clock] time. */
    public var startedAtMillis: Long = startedAtMillis
        private set

    /** How many times [resend] succeeded. */
    public var resendCount: Int = resendCount
        private set

    /** Resends still allowed. */
    public val resendsLeft: Int get() = (maxResends - resendCount).coerceAtLeast(0)

    /** Milliseconds until a resend is allowed; 0 once the countdown is over. */
    public fun remainingMillis(): Long =
        (startedAtMillis + durationMillis - clock.nowMillis()).coerceIn(0L, durationMillis)

    /** Whole seconds until a resend is allowed, rounded up. */
    public fun remainingSeconds(): Int = OtpTime.secondsCeil(remainingMillis()).toInt()

    /** Whether the countdown is still running. */
    public fun isRunning(): Boolean = remainingMillis() > 0L

    /** Whether the countdown is over and a resend is still allowed. */
    public fun canResend(): Boolean = !isRunning() && resendsLeft > 0

    /** Whether every resend has been used and the countdown is over. */
    public fun isExhausted(): Boolean = !isRunning() && resendsLeft == 0

    /** Records a resend and restarts the countdown. Returns `false`, changing nothing, when it is not allowed. */
    public fun resend(): Boolean {
        if (!canResend()) return false
        resendCount++
        startedAtMillis = clock.nowMillis()
        return true
    }

    /** Restarts the countdown without counting a resend, for example when a new verification starts. */
    public fun restart() {
        startedAtMillis = clock.nowMillis()
    }

    /** Restarts the countdown and forgets past resends. */
    public fun reset() {
        resendCount = 0
        startedAtMillis = clock.nowMillis()
    }

    /** The remaining time as `m:ss`. */
    public fun formatRemaining(): String = OtpTime.format(remainingMillis())
}
