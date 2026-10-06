package io.github.halilozel1903.otp.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OtpNormalizerTest {
    private val digits = OtpFormat(6)
    private val alnum = OtpFormat(6, OtpCharset.Alphanumeric)

    @Test
    fun keepsOnlyDigits() {
        assertEquals("123456", OtpNormalizer.normalize("12a3b4 56!", digits))
    }

    @Test
    fun stripsSpacesAndDashesFromPastedCodes() {
        assertEquals("123456", OtpNormalizer.normalize("123-456", digits))
        assertEquals("123456", OtpNormalizer.normalize(" 123 456 ", digits))
        assertEquals("123456", OtpNormalizer.normalize("12–34—56", digits))
    }

    @Test
    fun convertsFullWidthAndOtherScriptDigits() {
        assertEquals("482913", OtpNormalizer.normalize("４８２９１３", digits))
        assertEquals("4829", OtpNormalizer.normalize("٤٨٢٩", digits))
        assertEquals("12", OtpNormalizer.normalize("۱۲", digits))
    }

    @Test
    fun uppercasesAlphanumericCodes() {
        assertEquals("AB12CD", OtpNormalizer.normalize("ab-12 cd", alnum))
        assertEquals("XY", OtpNormalizer.normalize("ｘＹ", alnum))
    }

    @Test
    fun keepsCaseWhenAsked() {
        val caseSensitive = OtpFormat(4, OtpCharset.Alphanumeric, uppercase = false)
        assertEquals("aB3d", OtpNormalizer.normalize("aB3d", caseSensitive))
    }

    @Test
    fun rejectsNonLatinLettersInAlphanumeric() {
        assertNull(OtpNormalizer.normalizeChar('ş', alnum))
        assertNull(OtpNormalizer.normalizeChar('A', digits))
    }

    @Test
    fun sanitizeCutsToLength() {
        assertEquals("123456", OtpNormalizer.sanitize("1234567890", digits))
    }

    @Test
    fun validatesCompleteCodes() {
        assertTrue(OtpNormalizer.isValidCode("123456", digits))
        assertFalse(OtpNormalizer.isValidCode("12345", digits))
        assertFalse(OtpNormalizer.isValidCode("12345a", digits))
        assertFalse(OtpNormalizer.isValidCode("ab12cd", alnum))
        assertTrue(OtpNormalizer.isValidCode("AB12CD", alnum))
    }

    @Test
    fun separators() {
        assertTrue(OtpNormalizer.isSeparator(' '))
        assertTrue(OtpNormalizer.isSeparator('-'))
        assertFalse(OtpNormalizer.isSeparator('7'))
    }

    @Test
    fun formatValidatesLength() {
        assertFailsWith<IllegalArgumentException> { OtpFormat(0) }
        assertFailsWith<IllegalArgumentException> { OtpFormat(OtpFormat.MAX_LENGTH + 1) }
    }
}
