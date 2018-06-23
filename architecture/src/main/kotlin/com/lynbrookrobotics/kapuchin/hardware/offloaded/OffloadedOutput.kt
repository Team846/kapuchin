package com.lynbrookrobotics.kapuchin.hardware.offloaded

import info.kunalsheth.units.generated.Ampere
import info.kunalsheth.units.generated.Dimensionless

sealed class OffloadedOutput

data class VelocityOutput(override val gains: OffloadedPidGains, override val output: Double)
    : OffloadedOutput(), OffloadedPidControlLoop

data class PositionOutput(override val gains: OffloadedPidGains, override val output: Double)
    : OffloadedOutput(), OffloadedPidControlLoop

data class PercentOutput(val output: Dimensionless) : OffloadedOutput()

data class CurrentOutput(val output: Ampere) : OffloadedOutput()