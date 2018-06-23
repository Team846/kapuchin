package com.lynbrookrobotics.kapuchin.hardware.offloaded

interface OffloadedPidControlLoop {
    val gains: OffloadedPidGains
    val output: Double
}