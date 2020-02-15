package com.lynbrookrobotics.kapuchin.subsystems.storage

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.roundToInt

class CarouselConversions(hardware: CarouselHardware) : Named by Named("Conversions", hardware) {

    val rubberWheel by pref(1)
    val bigWheel by pref(10)
    val round by pref(72, Degree)
    val degreesPerRev by pref(360, Degree)

    val conversionFactor = rubberWheel / bigWheel

    private fun roundToMultiple(angle: Angle): Angle {

        val evenSplits = angle % round
        val remainder = angle - evenSplits * round.Degree
        val compressedRem = remainder / degreesPerRev
        val roundedRem = compressedRem.Each.roundToInt()

        when (roundedRem) {
            0 -> return angle
            else -> return angle + round
        }
    }
}