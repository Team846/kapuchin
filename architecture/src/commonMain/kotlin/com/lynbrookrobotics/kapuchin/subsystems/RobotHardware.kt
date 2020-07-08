package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*

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
abstract class RobotHardware<This> : Named by Named("override val name = ...")
        where This : RobotHardware<This> {

    /**
     * this subsystem's importance
     */
    abstract val priority: Priority

    /**
     * this subsystem's name
     */
    abstract override val name: String
}