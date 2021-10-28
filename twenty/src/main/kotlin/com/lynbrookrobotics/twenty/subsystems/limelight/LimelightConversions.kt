package com.lynbrookrobotics.twenty.subsystems.limelight

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class LimelightConversions(val hardware: LimelightHardware) : Named by Named("Conversions", hardware) {
    val targetHeight by pref(107, Inch)

    val mountingIncline by pref(38, Degree)
    val mounting by pref {
        val x by pref(0, Inch)
        val y by pref(0, Inch)
        val z by pref(24, Inch)
        ({ UomVector(x, y, z) })
    }

    val zoomOutSafetyZone by pref(40, Each)
    val zoomInSafetyZone by pref(10, Each)
    val zoomMultiplier by pref(2)

    val zoomInResolution by pref {
        val x by pref(240, Each)
        val y by pref(320, Each)
        ({ UomVector(x, y) })
    }
    val zoomInFov by pref {
        val x by pref(22.85, Degree)
        val y by pref(29.8, Degree)
        ({ UomVector(x, y) })
    }

    val zoomOutResolution by pref {
        val x by pref(720, Each)
        val y by pref(960, Each)
        ({ UomVector(x, y) })
    }
    val zoomOutFov by pref {
        val x by pref(45.7, Degree)
        val y by pref(59.6, Degree)
        ({ UomVector(x, y) })
    }
}