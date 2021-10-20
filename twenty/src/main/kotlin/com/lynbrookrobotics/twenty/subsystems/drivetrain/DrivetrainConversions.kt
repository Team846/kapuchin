package com.lynbrookrobotics.twenty.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class DrivetrainConversions(val hardware: DrivetrainHardware) : Named by Named("Conversions", hardware),
    GenericDrivetrainConversions {

    override val trackLength by pref(2, Foot)

    private val wheelRadius by pref {
        val left by pref(3, Inch)
        val right by pref(3, Inch)
        ({ TwoSided(left, right) })
    }

    private val leftTrim by pref(1.0)
    private val rightTrim by pref(1.0)

    val encoder by pref {
        val motorGear by pref(18)
        val stage1Gear by pref(50)
        val stage2Gear by pref(16)
        val wheelGear by pref(60)
        val resolution by pref(2048)
        val nativeEncoderCountMultiplier by pref(1)

        ({
            val stage1 = GearTrain(motorGear, stage1Gear)
            val stage2 = GearTrain(stage2Gear, wheelGear)

            val nativeResolution = resolution * nativeEncoderCountMultiplier
            val enc = EncoderConversion(
                nativeResolution,
                stage1.inputToOutput(1.Turn).let(stage2::inputToOutput)
            )

            val left = LinearOffloadedNativeConversion(
                ::p, ::p, ::p, ::p,
                nativeOutputUnits = 1023, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                nativeFeedbackUnits = nativeResolution,
                perFeedbackQuantity = wheelRadius.left * enc.angle(nativeResolution) * leftTrim / Radian,
                nativeTimeUnit = 100.milli(Second), nativeRateUnit = 1.Second
            )
            val right = left.copy(
                perFeedbackQuantity = wheelRadius.right * enc.angle(nativeResolution) * rightTrim / Radian
            )
            TwoSided(left, right)

        })
    }

    private var noTicksL = true
    private var noTicksR = true
    private var lastLeft = 0.Foot
    private var lastRight = 0.Foot

    val tracking = CircularArcTracking(Position(0.Foot, 0.Foot, 0.Degree))

    fun odometry(totalLeft: Length, totalRight: Length, bearing: Angle) {
        if (noTicksL && totalLeft != 0.Foot) log(Level.Debug) {
            "Received first left tick at ${currentTime withDecimals 2}"
        }.also { noTicksL = false }

        if (noTicksR && totalRight != 0.Foot) log(Level.Debug) {
            "Received first right tick at ${currentTime withDecimals 2}"
        }.also { noTicksR = false }

        tracking(
            totalLeft - lastLeft,
            totalRight - lastRight,
            bearing
        )
        lastLeft = totalLeft
        lastRight = totalRight
    }
}
