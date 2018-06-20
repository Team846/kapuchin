package com.lynbrookrobotics.kapuchin.hardware.offloaded

data class OffloadedPidConfig(val kP: Double, val kI: Double, val kD: Double, val kF: Double = 0.0)