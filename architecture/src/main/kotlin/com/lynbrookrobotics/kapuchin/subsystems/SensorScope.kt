package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.hardware.Sensor
import com.lynbrookrobotics.kapuchin.timing.Cancel
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import com.lynbrookrobotics.kapuchin.timing.blockingMutex
import com.lynbrookrobotics.kapuchin.timing.currentTime

class SensorScope(private val c: Component<*, *, *>) {

    fun close() = cleanup.forEach { it.cancel() }
    private var cleanup = emptyList<Cancel>()

    val <Input> Sensor<Input>.readOnTick
        get() = Sensor.UpdateSource(this, startUpdates = { _ ->
            blockingMutex(this) {
                cleanup += c.clock.runOnTick { value = optimizedRead(it, c.hardware.syncThreshold) }
            }
        })

    val <Input> Sensor<Input>.readWithEventLoop
        get() = Sensor.UpdateSource(this, startUpdates = { _ ->
            blockingMutex(this) {
                cleanup += EventLoop.runOnTick { value = optimizedRead(it, c.hardware.syncThreshold) }
            }
        })

    val <Input> Sensor<Input>.readEagerly
        get() = Sensor.UpdateSource(this, getValue = { _ ->
            optimizedRead(currentTime, c.hardware.syncThreshold).also { value = it }
        })
}