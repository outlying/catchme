package com.antyzero.catchme.core

import com.antyzero.catchme.core.find.RedColorBobberFinder
import com.antyzero.catchme.core.operator.OsxOperator
import com.antyzero.catchme.core.operator.WowOsOperator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.*

/**
 * Executes and starts catching process with given parameters
 */
class CatchMe(
    private val throwKey: String,
    private var sensitivity: Double = 0.33,
    private val detectionAreaSideLength: Int = 5
) {
    private val operator: WowOsOperator

    private var windowPosition: Pair<Int, Int>
    private var windowSize: Pair<Int, Int>

    private var preBobberImage: BufferedImage? = null
    private var postBobberImage: BufferedImage? = null

    private val failedCatches = AtomicInteger(0)

    private val bobberFinder = RedColorBobberFinder

    private val _message = MutableStateFlow("Start")
    val message: StateFlow<String>
        get() = _message

    init {
        operator = OsxOperator() // TODO select per system

        runBlocking {
            operator.focusWindow()
            windowPosition = operator.windowPosition()
            windowSize = operator.windowSize()
        }
    }

    suspend fun run() {

        val catched = catch(sensitivity)

        if (catched) {
            failedCatches.decrementAndGet()
        } else {
            failedCatches.incrementAndGet()
        }

        if (failedCatches.get() >= 3) {
            sensitivity *= 0.9
            sendMessage("Change sensitivity to $sensitivity")
            failedCatches.set(0)
        }

        delay(1000)
    }

    private suspend fun catch(sensitivity: Double): Boolean {
        neutralCursorPosition()
        throwBobber()
        val bobberPosition = findBobber()

        if (bobberPosition == null) {
            sendMessage("Bobber not found")
            return false
        } else {
            val (x, y) = bobberPosition.first + 4 to bobberPosition.second + 1
            sendMessage("Bobber position found at ($x, $y)")

            var previousArray: IntArray?
            var newArray: IntArray? = null

            var diff: Double
            var highestDiff = 0.0

            try {
                sendMessage("Waiting for a catch")
                withTimeout(20000) {

                    do {
                        previousArray = if (newArray == null) {
                            areaScreenshot(x, y, detectionAreaSideLength)
                        } else {
                            newArray
                        }

                        delay(250)
                        newArray = areaScreenshot(x, y, detectionAreaSideLength)

                        val previousAverage = previousArray!!.average()
                        val newAverage = newArray!!.average()

                        diff = (previousAverage - newAverage).absoluteValue

                        if (diff > highestDiff) {
                            highestDiff = diff
                        }

                    } while (diff < sensitivity)

                    delay(500)
                    moveMouse(x, y)
                    delay(500)

                    operator.leftClick()

                    sendMessage("Reel in")
                }
            } catch (e: Exception) {
                sendMessage("Catch failed")
                moveMouse(x, y)
                return false
            } finally {
                sendMessage("Highest detection: $highestDiff")
            }
        }

        return true
    }

    private fun areaScreenshot(centerX: Int, centerY: Int, sideLength: Int): IntArray {
        val halfSide = floor(sideLength.toDouble() / 2).toInt()
        val centerXOffset = max(centerX - halfSide, 0) + windowPosition.first
        val centerYOffset = max(centerY - halfSide, 0) + windowPosition.second
        val screenshot = operator.screenshot(centerXOffset to centerYOffset, sideLength to sideLength)
        return screenshot.getRGB(0, 0, sideLength, sideLength, IntArray(sideLength * sideLength), 0, sideLength)
    }

    private suspend fun neutralCursorPosition() {
        delay(500)
        moveMouse(0, 0)
        delay(500)
    }

    private suspend fun throwBobber() {
        preBobberImage = windowScreenShot()
        delay(500)
        sendMessage("Throwing bobber")
        operator.pressKeys(throwKey)
        delay(2500)
        postBobberImage = windowScreenShot()
    }

    private suspend fun findBobber(): Pair<Int, Int>? {
        val preBobber = preBobberImage ?: throw IllegalStateException("Missing pre throw image")
        val postBobber = postBobberImage ?: throw IllegalStateException("Missing post throw image")
        return bobberFinder.findBobber(preBobber, postBobber)
    }

    private fun moveMouse(position: Pair<Int, Int>) {
        moveMouse(position.first, position.second)
    }

    private fun moveMouse(x: Int, y: Int) {
        operator.moveMouse(windowPosition.first + x, windowPosition.second + y)
    }

    private fun windowScreenShot(
        position: Pair<Int, Int> = windowPosition,
        dimension: Pair<Int, Int> = windowSize
    ): BufferedImage {
        return operator.screenshot(position, dimension)
    }

    private fun sendMessage(message: CharSequence) {
        GlobalScope.launch(Dispatchers.IO) {
            delay(20)
            _message.value = message.toString()
        }
    }

    companion object {

        private const val SIDE_LENGTH = 25
        private val HALF_SIDE_OFFSET = floor((SIDE_LENGTH.toDouble() / 2)).toInt()

        private fun IntArray.average(): Double {
            val redList = map { Color(it).red }
            val sum = redList.sum()
            return sum / size.toDouble()
        }
    }
}
