package com.antyzero.catchme.gui

import javafx.scene.control.TextFormatter
import java.text.DecimalFormat
import java.text.ParsePosition


private var decimalFormat: DecimalFormat = DecimalFormat("#.0")

val OnlyDigitsFormatter = TextFormatter<TextFormatter.Change> {

    if (it.controlNewText.isEmpty()) {
        return@TextFormatter it
    }

    val parsePosition = ParsePosition(0)
    val formatted = decimalFormat.parse(it.controlNewText, parsePosition)

    if (formatted == null || parsePosition.index < it.controlNewText.length) {
        null
    } else {
        it
    }
}