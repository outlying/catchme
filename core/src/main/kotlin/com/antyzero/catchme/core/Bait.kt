package com.antyzero.catchme.core

data class Bait(
    val key: String,
   val minutes: Int) {

    init {
        require(key.length == 1) { "$key is too long, should be exactly one char" }
        require(minutes > 0) { "Time have to be greater than 0"}
    }
}
