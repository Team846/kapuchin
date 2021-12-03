package com.lynbrookrobotics.twenty.subsystems.carousel

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CarouselConversions(hardware: CarouselHardware) : Named by Named("Conversions", hardware) {
    val encoder by pref {
        val gearbox by pref {
            val motor by pref(1)
            val output by pref(20)
            ({ GearTrain(motor, output) })
        }
        val complianceWheelRadius by pref(1.25, Inch)
        val carouselRadius by pref(10, Inch)

        ({
            AngularOffloadedNativeConversion(
                ::p, ::p, ::p, ::p,
                nativeOutputUnits = 1, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                nativeFeedbackUnits = 1,
                perFeedbackQuantity = gearbox.inputToOutput(1.Turn) * complianceWheelRadius / carouselRadius,
                nativeTimeUnit = 1.Minute, nativeRateUnit = 1.milli(Second)
            )
        })
    }

    val ballIrRange by pref {
        val min by pref(87.5, Percent)
        val max by pref(100, Percent)
        ({ min..max })
    }
}