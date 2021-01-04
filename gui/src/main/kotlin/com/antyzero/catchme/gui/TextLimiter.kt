package com.antyzero.catchme.gui

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.TextField

class TextLimiter(
    private val textField: TextField,
    private val maxLength: Int
) : ChangeListener<String> {

    override fun changed(observableValue: ObservableValue<out String>?, p1: String?, p2: String?) {
        if (textField.text.length > maxLength) {
            val string: String = textField.text.substring(0, maxLength)
            textField.text = string
        }
    }
}