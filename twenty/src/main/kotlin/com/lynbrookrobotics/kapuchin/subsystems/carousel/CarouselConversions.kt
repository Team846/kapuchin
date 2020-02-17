package com.lynbrookrobotics.kapuchin.subsystems.carousel

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import edu.wpi.first.wpilibj.util.Color
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.pow

class CarouselConversions(hardware: CarouselHardware) : Named by Named("Conversions", hardware) {
    val encoder by pref {
        ({
            AngularOffloadedNativeConversion(::p, ::p, ::p, ::p,
                    nativeOutputUnits = 1, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                    nativeFeedbackUnits = 20,
                    perFeedbackQuantity = 1.Turn,
                    nativeTimeUnit = 1.Minute, nativeRateUnit = 1.milli(Second)
            )
        })
    }

    val ballIrRange by pref {
        val min by pref(50, Percent)
        val max by pref(90, Percent)
        ({ min..max })
    }
    val ballColor by pref {
        val r by pref(0.301)
        val g by pref(0.532)
        val b by pref(0.167)
        ({ Color(r, g, b) })
    }
    val ballColorTolerance by pref(0.1)

    fun accuracy(colorA: Color, colorB: Color = ballColor) =
            (colorA.red - colorB.red).pow(2) +
                    (colorA.green - colorB.green).pow(2) +
                    (colorA.blue - colorB.blue).pow(2)

    fun detectingBall(proximity: Dimensionless, color: Color) =
            proximity in ballIrRange && accuracy(color) < ballColorTolerance
}