package com.antyzero.catchme.core.find

import com.antyzero.catchme.core.arePixelsDifferent
import java.awt.Color
import java.awt.image.BufferedImage

/**
 * Looks
 */
object RedColorBobberFinder : BobberFinder {

    override suspend fun findBobber(preBobber: BufferedImage, postBobber: BufferedImage): Pair<Int, Int>? {

        val preRaster = preBobber.raster
        val postRaster = postBobber.raster

        val diffImage = BufferedImage(preBobber.width, preBobber.height, preBobber.type)

        var topRedPosition: Pair<Int, Int>? = null
        var topRedValue = 0
        var topGreenValue = 255
        var topBlueValue = 255

        for (x in 0 until preRaster.width) {
            for (y in 0 until preRaster.height) {

                val prePixel = preRaster.getPixel(x, y, DoubleArray(3))
                val postPixel = postRaster.getPixel(x, y, DoubleArray(3))

                val different = arePixelsDifferent(prePixel, postPixel)

                if (different) {
                    val normalized = postPixel.map { it.toInt() }
                    val color = Color(normalized[0], normalized[1], normalized[2])

                    diffImage.setRGB(x, y, color.rgb)

                    if (color.red >= topRedValue && (color.green < topGreenValue || color.blue < topBlueValue)) {
                        topRedValue = color.red
                        topGreenValue = color.green
                        topBlueValue = color.blue
                        topRedPosition = x to y
                    }
                } else {
                    diffImage.setRGB(x, y, 16777215)
                }
            }
        }

        return topRedPosition
    }
}