package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*

/**
 * Represents a robot subsystem's hardware.
 *
 * Intended for holding preferences, initializing hardware, and creating sensor objects.
 *
 * @author Kunal
 * @see Component
 * @see pref
 * @see HardwareInit
 * @see Sensor
 *
 * @param This type of child class
 * @param C type of this subsystem's component
 */
abstract class SubsystemHardware<This, C> : RobotHardware<This>()
        where This : SubsystemHardware<This, C>,
              C : Component<C, This, *> {

    /**
     * time between control loop updates
     */
    abstract val period: Time

    /**
     * sensor data timestamp jitter tolerance
     */
    abstract val syncThreshold: Time
}