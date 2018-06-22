package com.lynbrookrobotics.kapuchin.hardware.offloaded

data class OffloadedPidGains(val kP: Double, val kI: Double, val kD: Double, val kF: Double = 0.0)