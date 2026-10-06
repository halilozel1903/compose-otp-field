package io.github.halilozel1903.otp.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResendCountdownTest {

    @Test
    fun countsDownWithTheClock() {
        val clock = FakeClock()
        val countdown = ResendCountdown(60_000, clock = clock)
        assertEquals(60, countdown.remainingSeconds())
        assertEquals("1:00", countdown.formatRemaining())
        clock.advance(17_500)
        assertEquals(43, countdown.remainingSeconds())
        assertEquals("0:43", countdown.formatRemaining())
        assertFalse(countdown.canResend())
        assertFalse(countdown.resend())
        clock.advance(42_500)
        assertEquals(0, countdown.remainingSeconds())
        assertTrue(countdown.canResend())
    }

    @Test
    fun resendRestartsAndCounts() {
        val clock = FakeClock()
        val countdown = ResendCountdown(30_000, maxResends = 2, clock = clock)
        clock.advance(30_000)
        assertTrue(countdown.resend())
        assertEquals(1, countdown.resendCount)
        assertEquals(1, countdown.resendsLeft)
        assertTrue(countdown.isRunning())
        clock.advance(30_000)
        assertTrue(countdown.resend())
        clock.advance(30_000)
        assertFalse(countdown.canResend())
        assertTrue(countdown.isExhausted())
        assertFalse(countdown.resend())
        countdown.reset()
        assertEquals(0, countdown.resendCount)
        assertTrue(countdown.isRunning())
    }

    @Test
    fun restoresFromSavedState() {
        val clock = FakeClock(now = 100_000)
        val countdown = ResendCountdown(60_000, clock = clock, startedAtMillis = 80_000, resendCount = 1)
        assertEquals(40, countdown.remainingSeconds())
        assertEquals(1, countdown.resendCount)
    }

    @Test
    fun clockGoingBackwardsNeverExceedsDuration() {
        val clock = FakeClock(now = 10_000)
        val countdown = ResendCountdown(5_000, clock = clock)
        clock.now = 0
        assertEquals(5_000, countdown.remainingMillis())
    }

    @Test
    fun formatsDurations() {
        assertEquals("0:00", OtpTime.format(0))
        assertEquals("0:01", OtpTime.format(1))
        assertEquals("1:05", OtpTime.format(65_000))
        assertEquals("1:00:00", OtpTime.format(3_600_000))
    }
}
