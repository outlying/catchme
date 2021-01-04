package com.antyzero.catchme.core.find

import java.awt.image.BufferedImage

interface BobberFinder {

    suspend fun findBobber(preBobber: BufferedImage, postBobber: BufferedImage): Pair<Int, Int>?
}