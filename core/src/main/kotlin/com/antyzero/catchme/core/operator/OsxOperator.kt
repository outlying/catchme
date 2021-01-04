package com.antyzero.catchme.core.operator

import kotlinx.coroutines.delay
import java.awt.Color
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.InputStreamReader


class OsxOperator : WowOsOperator {

    private val runtime = Runtime.getRuntime()
    private val robot: Robot = Robot()

    init {
        robot.autoDelay = 40
        robot.isAutoWaitForIdle = true


    }

    override suspend fun focusWindow() {
        try {
            script("tell app \"Wow\" to activate")
            delay(2000L)
        } catch (e: Exception) {
            throw IllegalStateException("Window focus is not possible", e)
        }
    }

    override fun windowSize(): Pair<Int, Int> {
        val result = script(
            "tell application \"System Events\" to tell application process \"Wow\"",
            "get size of window 1",
            "end tell"
        )
        assert(result.isNotEmpty()) { "No window size output" }
        val pair = processNumbers(result.first())
        return pair.first to (pair.second - TOP_BAR_HEIGHT)
    }

    override fun windowPosition(): Pair<Int, Int> {
        val result = script(
            "tell application \"System Events\" to tell application process \"Wow\"",
            "get position of window 1",
            "end tell"
        )
        assert(result.isNotEmpty()) { "No window position output" }
        val pair = processNumbers(result.first())
        return pair.first to (pair.second + TOP_BAR_HEIGHT)
    }

    override fun screenshot(position: Pair<Int, Int>, dimensions: Pair<Int, Int>): BufferedImage {
        val area = Rectangle(position.first, position.second, dimensions.first, dimensions.second)
        return robot.createScreenCapture(area)
    }

    override fun pixel(position: Pair<Int, Int>): Color {
        return robot.getPixelColor(position.first, position.second)
    }

    override fun pressKeys(keys: String) {

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

    override fun moveMouse(x: Int, y: Int) {
        robot.mouseMove(x, y)
    }

    override fun leftClick() {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(200);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.delay(200);
    }

    private fun script(vararg script: String): List<String> {
        require(script.isNotEmpty()) { "At least one script line has to be provided" }

        val list = mutableListOf("osascript").apply {
            script.forEach { line ->
                add("-e")
                add(line)
            }
        }

        val process = runtime.exec(list.toTypedArray())

        val stdInput = BufferedReader(InputStreamReader(process.inputStream))
        val stdError = BufferedReader(InputStreamReader(process.errorStream))

        val output = mutableListOf<String>()
        val error = mutableListOf<String>()
        var line: String?
        while (!stdInput.readLine().also { line = it }.isNullOrBlank()) {
            output.add(line!!)
        }

        while (!stdError.readLine().also { line = it }.isNullOrBlank()) {
            error.add(line!!)
        }

        if (error.isNotEmpty()) {
            throw RuntimeException("Script [${script.joinToString(separator = "\n")}] execution error $error")
        }

        return output
    }

    companion object {

        private const val TOP_BAR_HEIGHT = 24

        fun processNumbers(input: String): Pair<Int, Int> {
            val dimensions = input.split(",").map { it.trim() }.map { it.toInt() }
            assert(dimensions.size == 2) { "We should only get two numbers" }
            return dimensions.first() to dimensions.last()
        }
    }
}