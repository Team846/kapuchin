package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * Utility to manage sensor use within robot choreographies
 *
 * Serves to setup sensor update strategies as needed by a robot choreography
 *
 * @author Kunal
 * @see Component
 * @see RobotHardware
 * @see SubsystemHardware
 * @see Sensor
 */
open class FreeSensorScope {

    /**
     * Stop all sensors in this routine from automatically updating
     */
    fun close() = cleanup.forEach { it.cancel() }

    internal val cleanup = mutableListOf<Cancel>()

    /**
     * Update this sensor's value on every `EventLoop` tick
     */
    fun <Input> Sensor<Input>.readWithEventLoop(syncThreshold: Time = 5.milli(Second)) =
        Sensor.UpdateSource(this, startUpdates = { _ ->
            cleanup += EventLoop.runOnTick { value = optimizedRead(it, syncThreshold) }
        })

    /**
     * Update this sensor's value every time the property is accessed
     */
    fun <Input> Sensor<Input>.readEagerly(syncThreshold: Time = 5.milli(Second)) =
        Sensor.UpdateSource(this, getValue = { _ ->
            optimizedRead(currentTime, syncThreshold).also { value = it }
        })

    /**
     * Rely on something else to update this sensor's value
     */
    val <Input> Sensor<Input>.getOld
        get() = Sensor.UpdateSource(this)
}