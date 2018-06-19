package com.lynbrookrobotics.kapuchin.delegates

import com.lynbrookrobotics.kapuchin.delegates.sensors.WithEventLoopSensor

interface WithEventLoop {
    companion object {
        private var allEventLoopSensors = emptySet<WithEventLoopSensor<*, *, *>>()
        fun update() = allEventLoopSensors.forEach { it.update() }
    }

    fun update()
}