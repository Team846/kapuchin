package com.lynbrookrobotics.kapuchin.timing

interface WithEventLoop {
    companion object {
        private var all = emptySet<WithEventLoop>()
        fun update() = all.forEach { it.update() }
    }

    fun update()
}