package com.lynbrookrobotics.kapuchin.hardware.offloaded

interface OffloadedPidControlLoop {
    val config: OffloadedPidConfig
    val output: Double
}