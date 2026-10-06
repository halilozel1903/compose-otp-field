package io.github.halilozel1903.otp.core

/**
 * Turns typed or pasted text into code characters.
 *
 * - Any Unicode decimal digit becomes its ASCII digit (`４２` → `42`, `٤٢` → `42`).
 * - Full-width Latin letters become ASCII letters, and letters are uppercased when the format asks for it.
 * - Separators people put in codes (spaces, dashes, dots, underscores) are dropped, so `123-456` and `123 456`
 *   paste as `123456`.
 * - Every other character is dropped.
 */
public object OtpNormalizer {

    /** The code character for [char], or `null` when it is not allowed in [format]. */
    public fun normalizeChar(char: Char, format: OtpFormat): Char? {
        if (Character.isDigit(char)) {
            val digit = Character.digit(char, 10)
            return if (digit in 0..9) '0' + digit else null
        }
        if (format.charset != OtpCharset.Alphanumeric) return null
        val ascii = when (char) {
            in 'Ａ'..'Ｚ' -> 'A' + (char - 'Ａ')
            in 'ａ'..'ｚ' -> 'a' + (char - 'ａ')
            else -> char
        }
        return when (ascii) {
            in 'A'..'Z' -> ascii
            in 'a'..'z' -> if (format.uppercase) ascii.uppercaseChar() else ascii
            else -> null
        }
    }

    /** All code characters in [text], in order, without a length limit. */
    public fun normalize(text: CharSequence, format: OtpFormat): String = buildString {
        for (char in text) {
            normalizeChar(char, format)?.let(::append)
        }
    }

    /** [normalize], cut to the code length. */
    public fun sanitize(text: CharSequence, format: OtpFormat): String = normalize(text, format).take(format.length)

    /** Whether [char] is a separator people write inside codes, such as the dash in `123-456`. */
    public fun isSeparator(char: Char): Boolean =
        char.isWhitespace() || char in SEPARATORS

    /** Whether [text] is a complete, valid code for [format]. */
    public fun isValidCode(text: CharSequence, format: OtpFormat): Boolean =
        text.length == format.length && text.all { normalizeChar(it, format) == it }

    private const val SEPARATORS = "-‐‑‒–—―−_.·・ー－"
}
