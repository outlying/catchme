package com.antyzero.catchme.gui

import javafx.scene.control.TextField
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextBoundsType
import kotlin.math.ceil

object TextUtils {

    private val helper: Text = Text()
    private val DEFAULT_WRAPPING_WIDTH = helper.wrappingWidth
    private val DEFAULT_LINE_SPACING = helper.lineSpacing
    private val DEFAULT_TEXT: String = helper.text
    private val DEFAULT_BOUNDS_TYPE: TextBoundsType = helper.boundsType

    fun computeTextWidth(font: Font, text: String, help0: Double): Double {
        helper.text = text
        helper.font = font
        helper.wrappingWidth = 0.0
        helper.lineSpacing = 0.0
        var d = helper.prefWidth(-1.0).coerceAtMost(help0)
        helper.wrappingWidth = ceil(d)
        d = ceil(helper.layoutBounds.width)
        helper.wrappingWidth = DEFAULT_WRAPPING_WIDTH
        helper.lineSpacing = DEFAULT_LINE_SPACING
        helper.text = DEFAULT_TEXT
        return d
    }

    fun TextField.computeTextWidth(help0: Double): Double {
        return TextUtils.computeTextWidth(this.font, this.text, help0)
    }
}