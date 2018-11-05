package com.lynbrookrobotics.kapuchin.hardware.offloaded

import info.kunalsheth.units.generated.Dimensionless
import info.kunalsheth.units.generated.I

sealed class OffloadedOutput

data class VelocityOutput(override val gains: OffloadedPidGains, override val output: Double)
    : OffloadedOutput(), OffloadedPidControlLoop

data class PositionOutput(override val gains: OffloadedPidGains, override val output: Double)
    : OffloadedOutput(), OffloadedPidControlLoop

data class PercentOutput(val output: Dimensionless) : OffloadedOutput()

data class CurrentOutput(val output: I) : OffloadedOutput()