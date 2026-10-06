package io.github.halilozel1903.otp.core

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class PinPadLayoutTest {

    @Test
    fun shuffledKeepsAllDigits() {
        val keys = PinPadLayout.shuffled(Random(7))
        assertEquals(PinPadLayout.Standard.sorted(), keys.sorted())
        assertEquals(keys, PinPadLayout.shuffled(Random(7)))
    }

    @Test
    fun letters() {
        assertEquals("ABC", PinPadLayout.lettersFor('2'))
        assertEquals("WXYZ", PinPadLayout.lettersFor('9'))
        assertEquals("", PinPadLayout.lettersFor('0'))
    }
}
