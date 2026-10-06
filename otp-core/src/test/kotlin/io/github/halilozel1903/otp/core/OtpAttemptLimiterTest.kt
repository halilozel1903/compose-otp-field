package io.github.halilozel1903.otp.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OtpAttemptLimiterTest {

    @Test
    fun countsDownAttempts() {
        val limiter = OtpAttemptLimiter(maxAttempts = 3, clock = FakeClock())
        assertEquals(3, limiter.remainingAttempts)
        assertEquals(OtpAttemptResult.Retry(2), limiter.recordFailure())
        assertEquals(OtpAttemptResult.Retry(1), limiter.recordFailure())
        assertEquals(1, limiter.remainingAttempts)
        assertTrue(limiter.canAttempt())
    }

    @Test
    fun locksOutAndExpires() {
        val clock = FakeClock()
        val limiter = OtpAttemptLimiter(maxAttempts = 2, lockoutMillis = 30_000, clock = clock)
        limiter.recordFailure()
        assertEquals(OtpAttemptResult.LockedOut(30_000, 1), limiter.recordFailure())
        assertTrue(limiter.isLockedOut)
        assertEquals(0, limiter.remainingAttempts)
        clock.advance(10_000)
        assertEquals(20_000, limiter.lockoutRemainingMillis())
        // Failures during a lockout do not extend it.
        assertEquals(OtpAttemptResult.LockedOut(20_000, 1), limiter.recordFailure())
        clock.advance(20_000)
        assertFalse(limiter.isLockedOut)
        assertEquals(2, limiter.remainingAttempts)
    }

    @Test
    fun backsOffOnRepeatedLockouts() {
        val clock = FakeClock()
        val limiter = OtpAttemptLimiter(
            maxAttempts = 1,
            lockoutMillis = 10_000,
            backoffMultiplier = 3.0,
            maxLockoutMillis = 60_000,
            clock = clock,
        )
        assertEquals(OtpAttemptResult.LockedOut(10_000, 1), limiter.recordFailure())
        clock.advance(10_000)
        assertEquals(OtpAttemptResult.LockedOut(30_000, 2), limiter.recordFailure())
        clock.advance(30_000)
        assertEquals(OtpAttemptResult.LockedOut(60_000, 3), limiter.recordFailure())
    }

    @Test
    fun successClearsEverything() {
        val clock = FakeClock()
        val limiter = OtpAttemptLimiter(maxAttempts = 1, lockoutMillis = 10_000, clock = clock)
        limiter.recordFailure()
        limiter.recordSuccess()
        assertFalse(limiter.isLockedOut)
        assertEquals(0, limiter.lockoutCount)
        assertEquals(1, limiter.remainingAttempts)
    }
}
