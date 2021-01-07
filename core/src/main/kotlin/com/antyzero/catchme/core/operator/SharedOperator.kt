package com.antyzero.catchme.core.operator

import java.awt.Color
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.image.BufferedImage

abstract class SharedOperator: WowOsOperator {

    private val robot: Robot = Robot()

    init {
        robot.autoDelay = 40
        robot.isAutoWaitForIdle = true
    }

    final override fun screenshot(position: Pair<Int, Int>, dimensions: Pair<Int, Int>): BufferedImage {
        val area = Rectangle(position.first, position.second, dimensions.first, dimensions.second)
        return robot.createScreenCapture(area)
    }

    final override fun pixel(position: Pair<Int, Int>): Color {
        return robot.getPixelColor(position.first, position.second)
    }

    final override fun pressKeys(keys: String) {

        val bytes: ByteArray = keys.toUpperCase().toByteArray()
        for (b in bytes) {
            var code = b.toInt()
            // keycode only handles [A-Z] (which is ASCII decimal [65-90])
            if (code in 97..122) code -= 32
            robot.delay(40)
            robot.keyPress(code)
            robot.delay(40)
            robot.keyRelease(code)
        }
    }

    final override fun moveMouse(x: Int, y: Int) {
        robot.mouseMove(x, y)
    }

    final override fun leftClick() {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(200);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.delay(200);
    }
}