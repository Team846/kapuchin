package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.hardware.HardwareInit
import com.lynbrookrobotics.kapuchin.hardware.Sensor

/**
 * Represents the context of an "important" object
 *
 * Intended to help eliminate logging boilerplate.
 * `Named` objects get special privileges. (see Extension Functions section)
 *
 * @author Kunal
 * @see Sensor
 * @see HardwareInit
 * @see log
 *
 * @property name name of the object to use for logging
 */
interface Named {
    val name: String

    companion object {
        /**
         * Public `Named` initializer
         *
         * Classes should implement `Named` via delegation to the output of this function
         *
         * @param shortName simple shortName of the object
         * @param parent scope this object belongs to
         * @return instance of `Named`
         */
        operator fun invoke(shortName: String, parent: Named? = null) = object : Named {
            override val name = nameLayer(parent, shortName)
        }
    }
}

/**
 * Generated a full-length name
 *
 * @author Kunal
 *
 * @param parent scope `child` belongs to
 * @param child object to generate full-length name for
 * @return full-length name
 */
expect fun nameLayer(parent: Named?, child: String): String