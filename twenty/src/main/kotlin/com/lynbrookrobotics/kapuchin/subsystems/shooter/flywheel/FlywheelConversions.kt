package com.lynbrookrobotics.kapuchin.subsystems.shooter.flywheel

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class FlywheelConversions(hardware: FlywheelHardware) : Named by Named("Conversions", hardware) {
    val encoder by pref {
        val motorSprocket by pref(45)
        val flywheelSprocket by pref(24)
        ({
            AngularOffloadedNativeConversion(::p, ::p, ::p, ::p,
                    nativeOutputUnits = 1, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                    nativeFeedbackUnits = 1,
                    perFeedbackQuantity = GearTrain(motorSprocket, flywheelSprocket).inputToOutput(1.Turn),
                    nativeTimeUnit = 1.Minute, nativeRateUnit = 1.milli(Second)
            )
        })
    }
}