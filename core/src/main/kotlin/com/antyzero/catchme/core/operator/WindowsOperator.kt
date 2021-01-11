package com.antyzero.catchme.core.operator

import com.antyzero.catchme.core.operator.WindowsOperator.User32.Companion.create
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import com.sun.jna.*
import com.sun.jna.platform.DesktopWindow
import com.sun.jna.win32.*
import com.sun.jna.win32.W32APIOptions
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary
import kotlinx.coroutines.delay
import java.awt.SystemColor.window
import com.sun.jna.platform.WindowUtils
import java.awt.Rectangle
import java.util.function.Consumer
import com.sun.jna.platform.win32.WinDef





object WindowsOperator : SharedOperator() {

    lateinit var window: DesktopWindow
    private var windowRect = Rectangle(0, 0, 0, 0) //needs to be final or effectively final for lambda
    private val runtime = Runtime.getRuntime()

    private val focusScript = File.createTempFile("focus", ".js")
    private val sizeScript = File.createTempFile("size", ".js")
    private val positionScript = File.createTempFile("position", ".js")

    init {
        focusScript.writeText("(new ActiveXObject(\"WScript.Shell\")).AppActivate(\"Discord\"); ")

        sizeScript.writeText("""
            Wscript.Echo "Like this?"
        """.trimIndent())

        positionScript.writeText(sizeScript.readText())
        WindowUtils.getAllWindows(true).forEach(Consumer { desktopWindow: DesktopWindow ->
            if (desktopWindow.title.contains("World of Warcraft")) {
                window = desktopWindow
                windowRect.setRect(desktopWindow.locAndSize)
            }
        })
        print(1)
    }

    override suspend fun focusWindow() {
        try {
            User32.INSTANCE.SetForegroundWindow(window.hwnd)
            //runScriptFile(focusScript)

        } catch (e: Exception) {
            throw IllegalStateException("Unable to focus window", e)
        }
    }

    override fun windowSize(): Pair<Int, Int> {
        return windowRect.width to windowRect.height
    }

    override fun windowPosition(): Pair<Int, Int> {
        return windowRect.x to windowRect.y
    }

    private fun runScriptFile(scriptFile: File): List<String> {
        val process = runtime.exec("cscript //nologo ${scriptFile.absoluteFile}")

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
            throw RuntimeException("Script [${scriptFile.readText()}] execution error $error")
        }

        return output
    }

    interface User32 : StdCallLibrary {

        fun FindWindow(lpClassName: String?, lpWindowName: String?): HWND?
        fun GetWindowRect(handle: HWND?, rect: IntArray?): Int
        fun SetFocus(hWnd: HWND?): HWND?
        fun SetForegroundWindow(hWnd: HWND?): Boolean

        companion object {

            val INSTANCE by lazy { create() }

            private fun create(): User32 = try {
                val user32 = Native.load(
                    "user32", User32::class.java,
                    W32APIOptions.DEFAULT_OPTIONS
                ) as User32

                user32
            } catch (e: Exception) {
                throw IllegalStateException("Unable to create User32", e)
            }
        }
    }
}