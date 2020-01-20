package com.lynbrookrobotics.kapuchin.subsystems.limelight

import info.kunalsheth.units.generated.*

data class LimelightReading(val tx: Angle, val ty: Angle,
                     val tx0: Angle, val ty0: Angle,
                     val thor: Double, val tvert: Double,
                     val ta: Double)