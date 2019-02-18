package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*

/**
 * Utility to manage sensor use within subsystem routines
 *
 * Serves to setup and cleanup sensor strategies styles as needed by a subsystem routine
 *
 * @author Kunal
 * @see Component
 * @see RobotHardware
 * @see SubsystemHardware
 * @see Sensor
 *
 * @param c this subsystem's component
 */
class BoundSensorScope internal constructor(private val c: Component<*, *, *>) : FreeSensorScope() {

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
        get() = readWithEventLoop(c.hardware.syncThreshold)

    /**
     * Update this sensor's value every time the property is accessed
     */
    val <Input> Sensor<Input>.readEagerly
        get() = readEagerly(c.hardware.syncThreshold)
}