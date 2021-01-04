package com.antyzero.catchme.core

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random


fun BufferedImage.save(file: File = File.createTempFile(Random.nextInt().toString(), ".jpg")) {
    println(file)
    ImageIO.write(this, "jpg", file)
}