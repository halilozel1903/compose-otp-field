package io.github.halilozel1903.otp.core

class FakeClock(var now: Long = 1_000L) : OtpClock {
    override fun nowMillis(): Long = now

    fun advance(millis: Long) {
        now += millis
    }
}
