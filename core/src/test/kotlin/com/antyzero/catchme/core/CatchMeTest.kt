package com.antyzero.catchme.core

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import javax.sound.sampled.*


class CatchMeTest {

    // @Test
    internal fun run() = runBlocking {


        val catchMe = CatchMe(
            throwKey = "q",
            sensitivity = 0.7,
            detectionAreaSideLength = 5
        )

        (1..6).forEach { _ ->
            catchMe.run()
        }
    }

    // @Test
    internal fun audioTest() {

        val audioFormat = AudioFormat(1000f, 100, 2, false, false)
        val info = DataLine.Info(TargetDataLine::class.java, audioFormat)

        if (!AudioSystem.isLineSupported(info)) {
            print("Line unsupported")
        }
        val line: TargetDataLine = try {
            (AudioSystem.getLine(info) as TargetDataLine).also { it.open(audioFormat) }
        } catch (e: LineUnavailableException) {
            throw IllegalStateException("Line failed", e)
        }

        print(line)
    }
}