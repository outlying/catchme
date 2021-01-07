package com.antyzero.catchme.core.operator

import com.sun.jna.Native
import com.sun.jna.win32.W32APIOptions.DEFAULT_OPTIONS
import com.sun.jna.win32.W32APIOptions.DEFAULT_OPTIONS

import com.sun.jna.win32.W32APIOptions







object WindowsOperator : SharedOperator() {

    override suspend fun focusWindow() {
    }

    override fun windowSize(): Pair<Int, Int> {
        TODO("Not yet implemented")
    }

    override fun windowPosition(): Pair<Int, Int> {
        TODO("Not yet implemented")
    }

    interface User32 : W32APIOptions {

        companion object {
            val instance = Native.loadLibrary(
                "user32",
                User32::class.java, DEFAULT_OPTIONS
            ) as User32
            const val SW_SHOW = 1
        }
    }
}