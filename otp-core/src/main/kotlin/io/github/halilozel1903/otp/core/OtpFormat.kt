package io.github.halilozel1903.otp.core

/** Which characters a code may contain. */
public enum class OtpCharset {
    /** `0` to `9` only. Any Unicode decimal digit (full-width, Arabic-Indic, ...) is converted to ASCII. */
    Digits,

    /** `0` to `9` and the Latin letters `A` to `Z`. Letters are uppercased unless [OtpFormat.uppercase] is off. */
    Alphanumeric,
}

/**
 * The shape of a one-time code: how many characters it has and which ones are allowed.
 *
 * @property length number of cells, at least 1.
 * @property charset allowed characters.
 * @property uppercase uppercase letters of an [OtpCharset.Alphanumeric] code. Turn it off for case-sensitive codes.
 */
public data class OtpFormat(
    val length: Int = 6,
    val charset: OtpCharset = OtpCharset.Digits,
    val uppercase: Boolean = true,
) {
    init {
        require(length in 1..MAX_LENGTH) { "length must be in 1..$MAX_LENGTH, was $length" }
    }

    public companion object {
        /** The longest code this library lays out. */
        public const val MAX_LENGTH: Int = 32

        /** A six digit code, the most common SMS and authenticator format. */
        public val SixDigits: OtpFormat = OtpFormat(6)

        /** A four digit PIN. */
        public val FourDigitPin: OtpFormat = OtpFormat(4)
    }
}
