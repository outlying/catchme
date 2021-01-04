package com.antyzero.catchme.gui

import com.antyzero.catchme.core.CatchMe
import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.time.LocalTime

class CatchMeApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Main)

    private val keyField by lazy {
        TextField().apply {
            textProperty().addListener(TextLimiter(this, 1))
            maxWidth = 30.0
            minWidth = 30.0
            alignment = Pos.CENTER
        }
    }
    private val fishButton by lazy {
        Button().apply {
            isDisable = true
            text = "Fish"
        }
    }
    private val loggerArea by lazy {
        TextArea().apply {
            isDisable = true
            style = "-fx-font-family: monospace"

            textProperty().addListener { _, _, _ ->
                this.scrollTop = Double.MAX_VALUE
            }
        }
    }

    override fun start(stage: Stage) {

        stage.isResizable = false

        keyField.textProperty().addListener { _, _, _ ->
            fishButton.isDisable = keyField.text.isNullOrBlank()
        }

        fishButton.onAction = EventHandler {
            applicationScope.launch(Dispatchers.Default) {
                try {
                    CatchMe(
                        throwKey = keyField.text.first().toString(),
                        detectionAreaSideLength = 3,
                        sensitivity = 50.0
                    ).apply {
                        launch {
                            message.collect { log ->
                                addToLog(log)
                            }
                        }

                        do {
                            run()
                        } while (true)
                    }
                } catch (e: Exception) {
                    addToLog(e.message.toString())
                } finally {
                    fishButton.isDisable = false
                }
            }
        }


        val root = StackPane().also { stackPane ->

            stackPane.children.add(VBox().also { vbox ->

                vbox.children.add(HBox().also { hbox ->
                    hbox.children.add(keyField)
                    hbox.children.add(fishButton)
                })

                vbox.children.add(loggerArea)
            })
        }

        val scene = Scene(root, 450.0, 250.0)

        stage.title = "CatchMe"
        stage.scene = scene
        stage.show()
    }

    private suspend fun addToLog(message: CharSequence) = withContext(Dispatchers.Default) {
        val time = LocalTime.now()
        val hour = time.hour
        val minute = time.minute.toString().padStart(2, '0')
        val second = time.second.toString().padStart(2, '0')
        val line = "[$hour:$minute:$second] $message"
        val originalText = loggerArea.text
        if (originalText.isNotBlank()) {
            loggerArea.appendText("\n")
        }
        loggerArea.appendText(line)
    }

    override fun stop() {
        applicationScope.cancel("App ended")
        super.stop()
    }
}