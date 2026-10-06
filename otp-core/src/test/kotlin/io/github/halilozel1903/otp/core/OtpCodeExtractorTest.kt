package io.github.halilozel1903.otp.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OtpCodeExtractorTest {
    private val six = OtpCodeExtractor(OtpFormat(6))

    @Test
    fun findsAPlainCode() {
        assertEquals("482913", six.extract("Your Lumen Bank code is 482913. It expires in 10 minutes."))
    }

    @Test
    fun findsCodesWithASeparator() {
        assertEquals("482913", six.extract("Code: 482-913"))
        assertEquals("482913", six.extract("Code: 482 913, do not share it"))
    }

    @Test
    fun findsCodesNextToPunctuationAndPrefixes() {
        assertEquals("104257", six.extract("G-104257 is your verification code."))
        assertEquals("104257", six.extract("<#> 104257 is your code\nFA+9qCX9VSu"))
    }

    @Test
    fun skipsPhoneAndAccountNumbers() {
        assertNull(six.extract("Call +1 555 123 4567 if this was not you."))
        assertNull(six.extract("Account 12345678 was charged"))
        assertEquals("903711", six.extract("Card 1234567890123456: code 903711"))
    }

    @Test
    fun skipsCodesOfAnotherLength() {
        assertNull(six.extract("Your code is 4829"))
        assertEquals("4829", OtpCodeExtractor(OtpFormat(4)).extract("Your code is 4829"))
    }

    @Test
    fun convertsFullWidthDigits() {
        assertEquals("482913", six.extract("確認コード：４８２９１３"))
    }

    @Test
    fun customPatternWithGroup() {
        val extractor = OtpCodeExtractor(OtpFormat(6), Regex("""ref (\d{3}) code (\d{6})"""))
        assertEquals(null, extractor.extract("ref 123 code 456789")) // group 1 has three digits
        val second = OtpCodeExtractor(OtpFormat(6), Regex("""code (?<code>\d{6})"""))
        assertEquals("456789", second.extract("ref 123 code 456789"))
    }

    @Test
    fun customPatternWithoutGroupUsesWholeMatch() {
        val extractor = OtpCodeExtractor(OtpFormat(6), Regex("""\d{3}-\d{3}"""))
        assertEquals("111222", extractor.extract("pin 111-222"))
    }

    @Test
    fun extractAllReturnsEveryCode() {
        assertEquals(listOf("111111", "222222"), six.extractAll("old 111111, new 222222"))
    }

    @Test
    fun alphanumericCodesNeedADigit() {
        val alnum = OtpCodeExtractor(OtpFormat(6, OtpCharset.Alphanumeric))
        assertEquals("K7Q2ZX", alnum.extract("PLEASE VERIFY with K7Q2ZX now"))
        assertEquals("AB12CD", alnum.extract("your code: ab12cd"))
        assertNull(alnum.extract("PLEASE VERIFY ACCESS"))
    }

    @Test
    fun emptyInput() {
        assertNull(six.extract(null))
        assertNull(six.extract(""))
    }
}
