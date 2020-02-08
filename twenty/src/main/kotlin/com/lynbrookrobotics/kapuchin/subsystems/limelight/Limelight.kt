package com.lynbrookrobotics.kapuchin.subsystems.limelight

import info.kunalsheth.units.generated.*

data class LimelightReading(
        val tx: Angle, val ty: Angle,
        val tx0: Dimensionless, val ty0: Dimensionless,
        val thor: Dimensionless, val tvert: Dimensionless,
        val ta: Dimensionless, val pipeline: Pipeline?
)
