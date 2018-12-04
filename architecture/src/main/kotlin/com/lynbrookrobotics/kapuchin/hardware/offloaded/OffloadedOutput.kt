package com.lynbrookrobotics.kapuchin.hardware.offloaded

import info.kunalsheth.units.generated.Dimensionless
import info.kunalsheth.units.generated.I

/**
 * Represents an CAN based electronic speed controller output
 *
 * @author Kunal
 * @see lazyOffloadedGainWriter
 */
sealed class OffloadedOutput

/**
 * Represents an offloaded velocity control loop
 *
 * @author Kunal
 * @see PositionOutput
 * @see OffloadedPidGains
 *
 * @property gains PIDF velocity control gains
 * @property output target velocity
 */
data class VelocityOutput(override val gains: OffloadedPidGains, override val output: Double)
    : OffloadedOutput(), OffloadedPidControlLoop

/**
 * Represents an offloaded position control loop
 *
 * @author Kunal
 * @see VelocityOutput
 * @see OffloadedPidGains
 *
 * @property gains PID position control gains
 * @property output target position
 */
data class PositionOutput(override val gains: OffloadedPidGains, override val output: Double)
    : OffloadedOutput(), OffloadedPidControlLoop

/**
 * Represents an electronic speed controller percent output
 *
 * @author Kunal
 *
 * @property output duty cycle
 */
data class PercentOutput(val output: Dimensionless) : OffloadedOutput()

/**
 * Represents an electronic speed controller current output
 *
 * @author Kunal
 *
 * @property output target current draw
 */
data class CurrentOutput(val output: I) : OffloadedOutput()