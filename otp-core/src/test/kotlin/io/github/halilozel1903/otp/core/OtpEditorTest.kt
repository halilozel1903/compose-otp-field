package io.github.halilozel1903.otp.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OtpEditorTest {
    private val editor = OtpEditor(OtpFormat(6))

    @Test
    fun typingFillsCellsInOrder() {
        var state = OtpInputState()
        for (c in "4829") state = editor.input(state, c.toString())
        assertEquals(OtpInputState("4829", 4), state)
        assertEquals(4, editor.activeCell(state))
        assertFalse(editor.isComplete(state))
    }

    @Test
    fun ignoresInvalidCharacters() {
        val state = OtpInputState("12", 2)
        assertEquals(state, editor.input(state, "x"))
    }

    @Test
    fun completesAndHasNoActiveCell() {
        val state = editor.input(OtpInputState("12345", 5), "6")
        assertEquals(OtpInputState("123456", 6), state)
        assertTrue(editor.isComplete(state))
        assertNull(editor.activeCell(state))
        // Typing into a complete code changes nothing.
        assertEquals(state, editor.input(state, "7"))
    }

    @Test
    fun typingOverATappedCellReplacesIt() {
        val tapped = editor.tap(OtpInputState("4829", 4), 1)
        assertEquals(1, tapped.cursor)
        assertEquals(OtpInputState("4029", 2), editor.input(tapped, "0"))
    }

    @Test
    fun tappingAnEmptyCellGoesToTheFirstEmptyOne() {
        assertEquals(2, editor.tap(OtpInputState("48", 2), 5).cursor)
        assertEquals(0, editor.tap(OtpInputState("", 0), 3).cursor)
    }

    @Test
    fun pasteFillsFromCursorAndTrimsOverflow() {
        assertEquals(OtpInputState("123456", 6), editor.paste(OtpInputState("12", 2), "3456789".take(4)))
        assertEquals(OtpInputState("129876", 6), editor.paste(OtpInputState("12", 2), "98 76 5"))
        assertEquals(OtpInputState("1999", 4), editor.paste(OtpInputState("1234", 1), "999"))
    }

    @Test
    fun pastingAFullCodeReplacesEverything() {
        assertEquals(OtpInputState("482913", 6), editor.paste(OtpInputState("12", 1), "482-913"))
        assertEquals(OtpInputState("482913", 6), editor.paste(OtpInputState(), "Code: 4829137"))
    }

    @Test
    fun backspaceOnEmptyCellDeletesThePreviousOne() {
        assertEquals(OtpInputState("482", 3), editor.backspace(OtpInputState("4829", 4)))
        assertEquals(OtpInputState("12345", 5), editor.backspace(OtpInputState("123456", 6)))
    }

    @Test
    fun backspaceOnFilledCellClearsIt() {
        assertEquals(OtpInputState("489", 2), editor.backspace(OtpInputState("4829", 2)))
        assertEquals(OtpInputState("829", 0), editor.backspace(OtpInputState("4829", 0)))
    }

    @Test
    fun backspaceOnEmptyFieldDoesNothing() {
        assertEquals(OtpInputState(), editor.backspace(OtpInputState()))
    }

    @Test
    fun stateOfSanitizes() {
        assertEquals(OtpInputState("123456", 6), editor.stateOf("12-34-56-78"))
    }

    @Test
    fun textChangeInsertAtCursorOverwrites() {
        // The keyboard inserted "9" before index 1; the editor writes over cell 1.
        assertEquals(OtpInputState("1934", 2), editor.applyTextChange(OtpInputState("1234", 1), "19234"))
    }

    @Test
    fun textChangeAppend() {
        assertEquals(OtpInputState("1235", 4), editor.applyTextChange(OtpInputState("123", 3), "1235"))
    }

    @Test
    fun textChangeSingleDeletionIsBackspace() {
        assertEquals(OtpInputState("123", 3), editor.applyTextChange(OtpInputState("1234", 4), "123"))
        // The keyboard deleted the character before the cursor, but the filled active cell is the one cleared.
        assertEquals(OtpInputState("134", 1), editor.applyTextChange(OtpInputState("1234", 1), "234"))
    }

    @Test
    fun textChangeRangeDeletion() {
        assertEquals(OtpInputState("", 0), editor.applyTextChange(OtpInputState("1234", 4), ""))
        assertEquals(OtpInputState("14", 1), editor.applyTextChange(OtpInputState("1234", 4), "14"))
    }

    @Test
    fun textChangeAutofillReplacesAll() {
        assertEquals(OtpInputState("482913", 6), editor.applyTextChange(OtpInputState("12", 2), "482913"))
    }

    @Test
    fun textChangeReplacement() {
        assertEquals(OtpInputState("1734", 2), editor.applyTextChange(OtpInputState("1234", 2), "1734"))
    }

    @Test
    fun textChangeWithInvalidCharactersIsRejected() {
        val state = OtpInputState("12", 2)
        assertEquals(state, editor.applyTextChange(state, "12a"))
    }

    @Test
    fun revealedCellForTypedCharacters() {
        assertEquals(3, editor.revealedCell("482", "4829"))
        assertEquals(1, editor.revealedCell("4829", "4029"))
        assertEquals(0, editor.revealedCell("", "4"))
        assertNull(editor.revealedCell("", "4829"))
        assertNull(editor.revealedCell("4829", "482"))
        assertNull(editor.revealedCell("4829", "4829"))
    }

    @Test
    fun alphanumericEditing() {
        val alnum = OtpEditor(OtpFormat(4, OtpCharset.Alphanumeric))
        assertEquals(OtpInputState("AB1", 3), alnum.input(OtpInputState("AB", 2), "1"))
        assertEquals(OtpInputState("AB1C", 4), alnum.paste(OtpInputState(), "ab-1c"))
    }

    @Test
    fun readmeExample() {
        val format = OtpFormat(length = 6)
        assertEquals("482913", OtpNormalizer.normalize("４８２-９１３", format))
        val editor = OtpEditor(format)
        var state = OtpInputState("48", cursor = 2)
        state = editor.paste(state, "29 137")
        assertEquals(OtpInputState("482913", 6), state)
        state = editor.tap(state, 1)
        assertEquals(1, state.cursor)
        state = editor.backspace(state)
        assertEquals(OtpInputState("42913", 1), state)
        assertEquals("482913", OtpCodeExtractor(format).extract("Your Lumen code is 482-913. Don't share it."))
    }
}
