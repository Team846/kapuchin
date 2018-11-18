package com.lynbrookrobotics.kapuchin.hardware.offloaded

/**
 * Represents an offloaded control loop
 *
 * @author Kunal
 *
 * @property gains PID(F) control gains
 * @property output target
 */
interface OffloadedPidControlLoop {
    val gains: OffloadedPidGains
    val output: Double
}