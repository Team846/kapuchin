package com.lynbrookrobotics.twenty.subsystems.shooter.flywheel

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
            AngularOffloadedNativeConversion(
                ::p, ::p, ::p, ::p,
                nativeOutputUnits = 1, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                nativeFeedbackUnits = 1,
                perFeedbackQuantity = GearTrain(motorSprocket, flywheelSprocket, 1).inputToOutput(1.Turn),
                nativeTimeUnit = 1.Minute, nativeRateUnit = 1.milli(Second)
            )
        })
    }

    val rpmCurve by pref {
        val a by pref(2.7)
        val b by pref(-32.8)
        val c by pref(5767)
        ({
            fun(dist: Length): AngularVelocity {
                val d = dist.Foot
                val rpm = a * d * d + b * d + c // quadratic
                return rpm.Rpm
            }
        })
    }
}