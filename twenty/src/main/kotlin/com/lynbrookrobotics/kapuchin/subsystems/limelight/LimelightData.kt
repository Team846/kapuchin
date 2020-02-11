package com.lynbrookrobotics.kapuchin.subsystems.limelight

import info.kunalsheth.units.generated.*

data class LimelightReading(
        val tx: Angle, val ty: Angle,
        val tx0: Dimensionless, val ty0: Dimensionless,
        val thor: Pixel, val tvert: Pixel,
        val ta: Dimensionless, val pipeline: Pipeline?
)

enum class Pipeline(val number: Int) {
    ZoomOut(0), ZoomInPanHigh(1), ZoomInPanMid(2), ZoomInPanLow(3), DriverStream(4)
}