package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*

/**
 * Utility to manage sensor use within subsystem routines
 *
 * Serves to setup and cleanup sensor update styles as needed by a subsystem routine
 *
 * @author Kunal
 * @see Component
 * @see SubsystemHardware
 * @see Sensor
 *
 * @param c this subsystem's component
 */
class SensorScope internal constructor(private val c: Component<*, *, *>) {

    /**
     * Stop all sensors in this routine from automatically updating
     */
    fun close() = cleanup.forEach { it.cancel() }

    private val cleanup = mutableListOf<Cancel>()

    /**
     * Update this sensor's value right before every control loop update
     */
    val <Input> Sensor<Input>.readOnTick
        get() = Sensor.UpdateSource(this, startUpdates = { _ ->
            cleanup += c.clock.runOnTick { value = optimizedRead(it, c.hardware.syncThreshold) }
        })

    /**
     * Update this sensor's value on every `EventLoop` tick
     */
    val <Input> Sensor<Input>.readWithEventLoop
        get() = Sensor.UpdateSource(this, startUpdates = { _ ->
            cleanup += EventLoop.runOnTick { value = optimizedRead(it, c.hardware.syncThreshold) }
        })

    /**
     * Update this sensor's value every time the property is accessed
     */
    val <Input> Sensor<Input>.readEagerly
        get() = Sensor.UpdateSource(this, getValue = { _ ->
            optimizedRead(currentTime, c.hardware.syncThreshold).also { value = it }
        })
}