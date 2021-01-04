package com.antyzero.catchme.core.operator

import java.awt.Color
import java.awt.image.BufferedImage

/**
 * Works with system
 */
interface WowOsOperator {

    suspend fun focusWindow()

    fun windowSize(): Pair<Int, Int>

    fun windowPosition(): Pair<Int, Int>

    fun screenshot(position: Pair<Int, Int>, dimensions: Pair<Int, Int>): BufferedImage

    fun pixel(position: Pair<Int, Int>): Color

    fun pixel(x: Int, y: Int) = pixel(x to y)

    fun pressKeys(keys: String)

    /**
     * Move withing WoW window
     */
    fun moveMouse(x: Int, y: Int)

    fun leftClick()
}