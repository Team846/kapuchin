package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

class DrivetrainConversions(val hardware: DrivetrainHardware) :
        Named by Named("Conversions", hardware),
        GenericDrivetrainConversions {

    override val trackLength by pref(2, Foot)
    private val wheelRadius by pref {
        val left by pref(3, Inch)
        val right by pref(3, Inch)
        ({ TwoSided(left, right) })
    }

    val encoder by pref {
        val encoderGear by pref(22)
        val wheelGear by pref(72)
        val resolution by pref(256)
        val nativeEncoderCountMultiplier by pref(4)
        ({
            val nativeResolution = resolution * nativeEncoderCountMultiplier
            val enc = EncoderConversion(
                    nativeResolution,
                    GearTrain(encoderGear, wheelGear).inputToOutput(1.Turn)
            )

            val left = LinearOffloadedNativeConversion(::p, ::p, ::p, ::p,
                    nativeOutputUnits = 1023, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                    nativeFeedbackUnits = nativeResolution,
                    perFeedbackQuantity = wheelRadius.left * enc.angle(nativeResolution) / Radian
            )
            val right = left.copy(
                    perFeedbackQuantity = wheelRadius.right * enc.angle(nativeResolution) / Radian
            )
            TwoSided(left, right)
        })
    }

    val escOdometry = EscOdometry(this)
    class EscOdometry(val conversions: DrivetrainConversions) : Named by Named("ESC Odometry", conversions) {
        private var noTicksL = true
        private var noTicksR = true
        val tracking = CircularArcTracking(Position(0.Foot, 0.Foot, 0.Degree))

        private var lastLeft = 0.Foot
        private var lastRight = 0.Foot
        operator fun invoke(totalLeft: Length, totalRight: Length, bearing: Angle) = conversions.run {
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
}
