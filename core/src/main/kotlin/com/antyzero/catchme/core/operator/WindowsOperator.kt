package com.antyzero.catchme.core.operator

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import com.profesorfalken.jpowershell.PowerShell

import com.profesorfalken.jpowershell.PowerShellResponse





object WindowsOperator : SharedOperator() {

    private val runtime = Runtime.getRuntime()

    private val focusScript = File.createTempFile("focus", ".js")
    private val sizeScript = File.createTempFile("size", ".js")
    private val positionScript = File.createTempFile("position", ".js")

    init {
        focusScript.writeText("(new ActiveXObject(\"WScript.Shell\")).AppActivate(\"Discord\"); ")

        sizeScript.writeText("""
            Wscript.Echo "Like this?"
        """.trimIndent())

        //positionScript.writeText("""""".trimIndent())

        positionScript.writeText(sizeScript.readText())
    }

    override suspend fun focusWindow() {
        try {
            runScriptFile(focusScript)
        } catch (e: Exception) {
            throw IllegalStateException("Unable to focus window", e)
        }
    }

    override fun windowSize(): Pair<Int, Int> {
        try {
            println(runScriptFile(focusScript))
            return 0 to 0
        } catch (e: Exception) {
            throw IllegalStateException("Unable to focus window", e)
        }
    }

    override fun windowPosition(): Pair<Int, Int> {

        val response = PowerShell.executeSingleCommand("Get-Process -ProcessName Wow").commandOutput

        print(response)

        return 0 to 0

        try {
            println(runScriptFile(focusScript))
            return 0 to 0
        } catch (e: Exception) {
            throw IllegalStateException("Unable to focus window", e)
        }
    }

    private fun runScriptFile(scriptFile: File): List<String> {
        val process = runtime.exec("cscript //nologo //B ${scriptFile.absoluteFile}")

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

    private const val sizePositionScript = """
    """
}