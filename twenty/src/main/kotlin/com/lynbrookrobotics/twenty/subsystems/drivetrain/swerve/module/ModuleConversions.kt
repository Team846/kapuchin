package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve.module

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainHardware
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class ModuleConversions(val hardware: ModuleHardware) : Named by Named("Conversions", hardware),
    GenericWheelConversions {
    override val wheelRadius by pref(3, Inch)
    private val trim by pref(1.0)

    val wheelEncoder by pref{
        val motorGear by pref(18)
        val stage1Gear by pref(50)
        val wheelGear by pref(60)
        val resolution by pref(2048)

        val nativeEncoderMultiplier by pref(1)
        ({
            val stage1 = GearTrain(motorGear, stage1Gear)
            val nativeResolution = resolution * nativeEncoderMultiplier

            val enc = EncoderConversion(
                nativeResolution,
                stage1.inputToOutput(1.Turn)
            )

            val encoder = LinearOffloadedNativeConversion(
                ::p, ::p, ::p, ::p,
                nativeOutputUnits = 1023, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                nativeFeedbackUnits = nativeResolution,
                perFeedbackQuantity = wheelRadius * enc.angle(nativeResolution) * trim / Radian,
                nativeTimeUnit = 100.milli(Second), nativeRateUnit = 1.Second
            )
            encoder
        })
    }

    val angleEncoder by pref {
        val gearbox by pref {
            val motor by pref(1)
            val output by pref(20)
            ({ GearTrain(motor, output) })
        }
        val motorGearRadius by pref(1.25, Inch)
        val outputGearRadius by pref(10, Inch)

        ({
            AngularOffloadedNativeConversion(
                ::p, ::p, ::p, ::p,
                nativeOutputUnits = 1, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                nativeFeedbackUnits = 1,
                perFeedbackQuantity = gearbox.inputToOutput(1.Turn) * motorGearRadius / outputGearRadius,
                nativeTimeUnit = 1.Minute, nativeRateUnit = 1.milli(Second)
            )
        })
    }
    
    private var ticks = true
    private var lastTicks = 0.Foot

    override fun toString() = name

    fun wheelOdometry(totalDist: Length){ //may not be needed
        if (ticks && totalDist != 0.Foot) log(Level.Debug) {
            "Received first tick from $name at ${currentTime withDecimals 2}"
        }.also { ticks = false }
    }
}