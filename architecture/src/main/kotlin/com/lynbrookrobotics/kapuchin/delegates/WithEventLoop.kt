package com.lynbrookrobotics.kapuchin.delegates

interface WithEventLoop {
    companion object {
        private var all = emptySet<WithEventLoop>()
        fun update() = all.forEach { it.update() }
    }

    fun update()
}