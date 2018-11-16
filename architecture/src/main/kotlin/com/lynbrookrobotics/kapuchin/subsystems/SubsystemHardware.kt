package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.hardware.HardwareInit
import com.lynbrookrobotics.kapuchin.hardware.Sensor
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.Time

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
abstract class SubsystemHardware<This, C> : Named by Named("override val name = ...")
        where This : SubsystemHardware<This, C>,
              C : Component<C, This, *> {

    /**
     * this subsystem's importance
     */
    abstract val priority: Priority

    /**
     * time between control loop updates
     */
    abstract val period: Time

    /**
     * sensor data timestamp jitter tolerance
     */
    abstract val syncThreshold: Time

    /**
     * this subsystem's name
     */
    abstract override val name: String
}