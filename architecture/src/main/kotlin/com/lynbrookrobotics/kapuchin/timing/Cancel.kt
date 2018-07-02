package com.lynbrookrobotics.kapuchin.timing

class Cancel(private val f: () -> Unit) {
    fun cancel() = f()
}