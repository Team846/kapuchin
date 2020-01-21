package com.lynbrookrobotics.kapuchin.subsystems.limelight

import info.kunalsheth.units.generated.*

data class LimelightReading(val tx: Angle, val ty: Angle,
                     val tx0: Double, val ty0: Double,
                     val thor: Double, val tvert: Double,
                     val ta: Double)