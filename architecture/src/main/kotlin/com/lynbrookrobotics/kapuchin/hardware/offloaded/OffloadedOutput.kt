package com.lynbrookrobotics.kapuchin.hardware.offloaded

import info.kunalsheth.units.generated.Ampere
import info.kunalsheth.units.generated.Volt

sealed class OffloadedOutput

data class VelocityOutput(override val config: OffloadedPidConfig, override val output: Double)
    : OffloadedOutput(), OffloadedPidControlLoop

data class PositionOutput(override val config: OffloadedPidConfig, override val output: Double)
    : OffloadedOutput(), OffloadedPidControlLoop

data class VoltageOutput(val output: Volt) : OffloadedOutput()

data class CurrentOutput(val output: Ampere) : OffloadedOutput()