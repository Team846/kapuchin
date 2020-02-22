package com.lynbrookrobotics.kapuchin.subsystems.shooter.turret

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class TurretConversions(hardware: TurretHardware) : Named by Named("Conversions", hardware) {
    val encoder by pref {
        val gearbox by pref {
            val input = 1
            val output = 10
            ({ GearTrain(input, output) })
        }
        val driveWheelRadius by pref(1, Inch)
        val lazySusanRadius by pref(13, Inch)

        ({
            AngularOffloadedNativeConversion(::p, ::p, ::p, ::p,
                    nativeOutputUnits = 1, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                    nativeFeedbackUnits = 1,
                    perFeedbackQuantity = gearbox.inputToOutput(1.Turn) * driveWheelRadius / lazySusanRadius,
                    nativeTimeUnit = 1.Minute, nativeRateUnit = 1.milli(Second)
            )
        })
    }

    val min by pref(-180, Degree)
    val max by pref(90, Degree)
}