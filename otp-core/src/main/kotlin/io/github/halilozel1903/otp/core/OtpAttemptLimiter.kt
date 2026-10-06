package io.github.halilozel1903.otp.core

/** What happened after a wrong code. */
public sealed interface OtpAttemptResult {
    /** The user may try again [remainingAttempts] more times before a lockout. */
    public data class Retry(val remainingAttempts: Int) : OtpAttemptResult

    /** Too many wrong codes: no attempts are allowed for [durationMillis]. */
    public data class LockedOut(val durationMillis: Long, val lockoutCount: Int) : OtpAttemptResult
}

/**
 * Counts wrong codes and locks entry after [maxAttempts] of them.
 *
 * A lockout lasts [lockoutMillis], multiplied by [backoffMultiplier] for each earlier lockout and capped at
 * [maxLockoutMillis]. When it ends, the user gets [maxAttempts] new attempts. A correct code ([recordSuccess])
 * clears everything.
 *
 * Client-side limits improve the experience; the server must enforce its own limits too.
 */
public class OtpAttemptLimiter(
    public val maxAttempts: Int = 3,
    public val lockoutMillis: Long = 30_000L,
    public val backoffMultiplier: Double = 2.0,
    public val maxLockoutMillis: Long = 15 * 60_000L,
    private val clock: OtpClock = OtpClock.Monotonic,
) {
    init {
        require(maxAttempts >= 1) { "maxAttempts must be at least 1, was $maxAttempts" }
        require(lockoutMillis >= 0) { "lockoutMillis must not be negative, was $lockoutMillis" }
        require(backoffMultiplier >= 1.0) { "backoffMultiplier must be at least 1, was $backoffMultiplier" }
        require(maxLockoutMillis >= lockoutMillis) { "maxLockoutMillis must be at least lockoutMillis" }
    }

    private var failures = 0
    private var lockedUntil: Long? = null

    /** How many lockouts happened since the last success or [reset]. */
    public var lockoutCount: Int = 0
        private set

    /** Wrong codes since the last success, lockout or [reset]. */
    public val failedAttempts: Int get() {
        expireLockout()
        return failures
    }

    /** Attempts left before the next lockout; 0 while locked out. */
    public val remainingAttempts: Int get() {
        expireLockout()
        return if (lockedUntil != null) 0 else maxAttempts - failures
    }

    /** Whether entry is locked right now. */
    public val isLockedOut: Boolean get() {
        expireLockout()
        return lockedUntil != null
    }

    /** Whether the user may submit a code now. */
    public fun canAttempt(): Boolean = !isLockedOut

    /** Milliseconds until the lockout ends; 0 when not locked out. */
    public fun lockoutRemainingMillis(): Long {
        expireLockout()
        val until = lockedUntil ?: return 0L
        return (until - clock.nowMillis()).coerceAtLeast(0L)
    }

    /** Records a wrong code. While locked out, nothing changes and the current lockout is returned. */
    public fun recordFailure(): OtpAttemptResult {
        expireLockout()
        lockedUntil?.let { until ->
            return OtpAttemptResult.LockedOut((until - clock.nowMillis()).coerceAtLeast(0L), lockoutCount)
        }
        failures++
        if (failures < maxAttempts) return OtpAttemptResult.Retry(maxAttempts - failures)
        val duration = nextLockoutMillis()
        lockoutCount++
        failures = 0
        lockedUntil = clock.nowMillis() + duration
        return OtpAttemptResult.LockedOut(duration, lockoutCount)
    }

    /** Records a correct code: clears failures and lockouts. */
    public fun recordSuccess() {
        reset()
    }

    /** Forgets all failures and lockouts. */
    public fun reset() {
        failures = 0
        lockoutCount = 0
        lockedUntil = null
    }

    private fun nextLockoutMillis(): Long {
        var duration = lockoutMillis.toDouble()
        repeat(lockoutCount) { duration *= backoffMultiplier }
        return duration.coerceAtMost(maxLockoutMillis.toDouble()).toLong()
    }

    private fun expireLockout() {
        val until = lockedUntil ?: return
        if (clock.nowMillis() >= until) {
            lockedUntil = null
            failures = 0
        }
    }
}
