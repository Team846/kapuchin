package com.lynbrookrobotics.kapuchin.hardware.offloaded

/**
 * Stores gains for offloaded PID control
 *
 * @author Kunal
 * @see OffloadedPidControlLoop
 * @see VelocityOutput
 * @see PositionOutput
 *
 * @property kP proportional gain
 * @property kI integral gain
 * @property kD derivative gain
 * @property kF feed forward term
 */
data class OffloadedPidGains(val kP: Double, val kI: Double, val kD: Double, val kF: Double = 0.0)