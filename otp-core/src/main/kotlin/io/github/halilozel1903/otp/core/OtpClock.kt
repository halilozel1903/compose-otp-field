package io.github.halilozel1903.otp.core

/** A source of time in milliseconds. Inject a fake one in tests; only differences between readings matter. */
public fun interface OtpClock {
    public fun nowMillis(): Long

    public companion object {
        /** A monotonic clock that is not affected by changes to the wall clock. */
        public val Monotonic: OtpClock = OtpClock { java.lang.System.nanoTime() / 1_000_000L }
    }
}

/** Formats durations for countdowns. */
public object OtpTime {
    /** Whole seconds left in [millis], rounded up so a countdown shows 1 until it reaches 0. */
    public fun secondsCeil(millis: Long): Long = if (millis <= 0L) 0L else (millis + 999L) / 1000L

    /** `m:ss`, or `h:mm:ss` from an hour on: `0:42`, `1:05`, `1:00:00`. Rounded up to the second. */
    public fun format(millis: Long): String {
        val total = secondsCeil(millis)
        val hours = total / 3600
        val minutes = (total % 3600) / 60
        val seconds = total % 60
        return if (hours > 0) {
            "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        } else {
            "$minutes:${seconds.toString().padStart(2, '0')}"
        }
    }
}
