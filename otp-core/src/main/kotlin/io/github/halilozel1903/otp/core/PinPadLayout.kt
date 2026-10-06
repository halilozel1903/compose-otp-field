package io.github.halilozel1903.otp.core

import kotlin.random.Random

/** The order of the ten digit keys on a PIN pad, laid out as three rows of three and a last row with one key. */
public object PinPadLayout {
    /** `1 2 3 / 4 5 6 / 7 8 9 / 0`, like a phone keypad. */
    public val Standard: List<Char> = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')

    /**
     * The ten digits in a random order, for keypads that should not reveal the PIN through touch positions.
     * Pass a seeded [random] for a stable order.
     */
    public fun shuffled(random: Random = Random.Default): List<Char> = Standard.shuffled(random)

    /** The letters printed under a digit key on phone keypads (`2` → `ABC`), empty for `0` and `1`. */
    public fun lettersFor(digit: Char): String = when (digit) {
        '2' -> "ABC"
        '3' -> "DEF"
        '4' -> "GHI"
        '5' -> "JKL"
        '6' -> "MNO"
        '7' -> "PQRS"
        '8' -> "TUV"
        '9' -> "WXYZ"
        else -> ""
    }
}
