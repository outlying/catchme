package com.antyzero.catchme.gui

import com.antyzero.catchme.core.Bait
import com.antyzero.catchme.core.CatchMe
import javafx.application.Application
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
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

    private val applicationScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var fishingJob: Job? = null

    private val bobberKeyField by lazy { createTextField() }
    private val baitKeyField by lazy { createTextField() }
    private val baitTimeField by lazy {
        createTextField(maxChars = 2).apply {
            textFormatter = OnlyDigitsFormatter
            text = "30"
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
            minHeight = 210.0
            prefHeight = Double.MAX_VALUE
            isDisable = true
            style = "-fx-font-family: monospace"

            textProperty().addListener { _, _, _ ->
                this.scrollTop = Double.MAX_VALUE
            }
        }
    }

    override fun start(stage: Stage) {

        stage.isResizable = false

        bobberKeyField.textProperty().addListener { _, _, _ ->
            fishButton.isDisable = bobberKeyField.text.isNullOrBlank()
        }

        fishButton.onAction = EventHandler {
            fishingJob?.cancel()
            fishingJob = applicationScope.launch(Dispatchers.Default) {
                try {
                    val bait = if(!(baitKeyField.text.isNullOrBlank() || baitTimeField.text.isNullOrBlank())) {
                        Bait(baitKeyField.text, baitTimeField.text.toInt())
                    } else {
                        null
                    }

                    CatchMe(
                        throwKey = bobberKeyField.text.first().toString(),
                        detectionAreaSideLength = 5,
                        threshold = 45.0,
                        bait = bait
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

            stackPane.children.add(VBox().also { topDown ->

                topDown.children.add(HBox().also { buttonsContainer ->

                    buttonsContainer.spacing = 4.0

                    buttonsContainer.children.addLabel("Bobber key")
                    buttonsContainer.children.add(bobberKeyField)
                    buttonsContainer.children.addLabel("Bait key")
                    buttonsContainer.children.add(baitKeyField)
                    buttonsContainer.children.addLabel("Bait time (min)")
                    buttonsContainer.children.add(baitTimeField)

                    buttonsContainer.children.add(fishButton)
                })

                topDown.children.add(loggerArea)
            })
        }

        val scene = Scene(root, 450.0, 250.0)

        stage.title = "CatchMe"
        stage.scene = scene
        stage.show()
    }

    private fun CoroutineScope.addToLog(message: CharSequence) = launch(Dispatchers.Default) {

        val time = LocalTime.now()
        val hour = time.hour
        val minute = time.minute.toString().padStart(2, '0')
        val second = time.second.toString().padStart(2, '0')
        val line = "[$hour:$minute:$second] $message\n"

        Platform.runLater {
            loggerArea.appendText(line)
        }
    }

    override fun stop() {
        applicationScope.cancel("App ended")
        super.stop()
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            launch(CatchMeApplication::class.java, *args)
        }

        private fun ObservableList<Node>.addLabel(text: String): Boolean {
            return add(Label(text).apply {
                padding = Insets(8.0)
            })
        }

        private fun createTextField(maxChars: Int = 1): TextField {
            return TextField().apply {
                textProperty().addListener(TextLimiter(this, maxChars))
                maxWidth = 500.0
                minWidth = 30.0
                prefWidth = TextUtils.computeTextWidth(font, "99", 0.0) + 20
                prefHeight = Double.MAX_VALUE
                alignment = Pos.CENTER
            }
        }
    }
}