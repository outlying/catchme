package com.antyzero.catchme.core

import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt

fun arePixelsDifferent(pre: DoubleArray, post: DoubleArray): Boolean {
    assert(pre.size == 3)
    assert(post.size == 3)

    val preLength = vectorLength(pre)
    val postLength = vectorLength(post)

    val postRed = post[0]
    val postGreen = post[1]
    val postBlue = post[2]

    return (preLength - postLength).absoluteValue >= 50 && postRed > 50 && postBlue < 50 && postGreen < 50
}

private fun vectorLength(vector: DoubleArray): Double {
    return sqrt(vector[0].pow(2) + vector[1].pow(2) + vector[2].pow(2))
}