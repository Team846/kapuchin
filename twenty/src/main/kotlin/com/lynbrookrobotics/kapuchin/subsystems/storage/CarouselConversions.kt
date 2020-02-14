package com.lynbrookrobotics.kapuchin.subsystems.storage

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CarouselConversions(hardware: CarouselHardware) : Named by Named("Conversions", hardware) {
    val encoder by pref {
        val rubberWheel by pref(22)
        val carouselGear by pref(72)
        val resolution by pref(256)

        val nativeEncoderCountMultiplier by pref(4)

        ({
            val gearing = GearTrain(rubberWheel, carouselGear)
            val nativeResolution = nativeEncoderCountMultiplier * resolution

            val enc = EncoderConversion(
                    resolution,
                    gearing.inputToOutput(1.Turn)
            )

            val nat = LinearOffloadedNativeConversion(::p, ::p, ::p, ::p,
                    nativeOutputUnits = 1023, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                    nativeFeedbackUnits = nativeResolution,
                    perFeedbackQuantity = TODO()
            )

            enc to nat
        })
    }

    val encoderConversion get() = encoder.first
    val nativeConversion get() = encoder.second

    fun toLeftPosition(
            ticks: Int, conv: EncoderConversion = encoderConversion
    ): Length = TODO()

    fun toRightPosition(
            ticks: Int, conv: EncoderConversion = encoderConversion
    ): Length = TODO()

}