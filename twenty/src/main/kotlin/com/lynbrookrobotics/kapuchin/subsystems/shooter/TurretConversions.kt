package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class TurretConversions(hardware: TurretHardware) : Named by Named("Conversions", hardware) {
    val encoder by pref {
        val encoderGear by pref(22)
        val turretGear by pref(72)
        val resolution by pref(256)

        val nativeEncoderCountMultiplier by pref(4)

        ({
            val gearing = GearTrain(encoderGear, turretGear)

            val enc = EncoderConversion(
                    resolution,
                    gearing.inputToOutput(1.Turn)
            )

            val nat = LinearOffloadedNativeConversion(::p, ::p, ::p, ::p,
                    nativeOutputUnits = 1023, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                    nativeFeedbackUnits = nativeResolution,

        })
    }
}