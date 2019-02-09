package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class DrivetrainConversions(val hardware: DrivetrainHardware) : Named by Named("Conversions", hardware) {
    private val wheelRadius by pref(3, Inch)

    private val leftTrim by pref(1.0018314)
    private val flipLeftPosition by pref(false)
    private val flipRightPosition by pref(false)
    private val flipLeftSpeed by pref(false)
    private val flipRightSpeed by pref(true)
    private val flipOdometryLeft by pref(false)
    private val flipOdometryRight by pref(true)
    private val rightTrim by pref(1.00621994)
    private val trackLength by pref(2.05, Foot)

    val nativeEncoderCountMultiplier by pref(4)

    val encoder by pref {
        val encoderGear by pref(18)
        val wheelGear by pref(74)
        val resolution by pref(1024)
        ({
            val gearing = GearTrain(encoderGear, wheelGear)
            val nativeResolution = nativeEncoderCountMultiplier * resolution

            val enc = EncoderConversion(
                    resolution,
                    gearing.inputToOutput(1.Turn)
            )

            val nat = LinearOffloadedNativeConversion(::div, ::div, ::times, ::times,
                    nativeOutputUnits = 1023, perOutputQuantity = hardware.operatingVoltage,
                    nativeFeedbackUnits = nativeResolution,
                    perFeedbackQuantity = avg(
                            toLeftPosition(nativeResolution, enc),
                            toRightPosition(nativeResolution, enc)
                    )
            )

            enc to nat
        })
    }
    val encoderConversion get() = encoder.first
    val nativeConversion get() = encoder.second


    fun toLeftPosition(
            ticks: Int, conv: EncoderConversion = encoderConversion
    ): Length = wheelRadius * conv.angle(ticks.toDouble()) * leftTrim / Radian
            .let { if (flipLeftPosition) -it else it }

    fun toRightPosition(
            ticks: Int, conv: EncoderConversion = encoderConversion
    ): Length = wheelRadius * conv.angle(ticks.toDouble()) * rightTrim / Radian
            .let { if (flipRightPosition) -it else it }

    fun toLeftSpeed(
            period: Time, conv: EncoderConversion = encoderConversion
    ): Velocity =
            if (period.isZero) 0.FootPerSecond
            else wheelRadius * conv.angle(1.0) / period * leftTrim / Radian
                    .let { if (flipLeftSpeed) -it else it }
                    .let { if (leftMovingForward) it else -it }

    fun toRightSpeed(
            period: Time, conv: EncoderConversion = encoderConversion
    ): Velocity =
            if (period.isZero) 0.FootPerSecond
            else wheelRadius * conv.angle(1.0) / period * rightTrim / Radian
                    .let { if (flipRightSpeed) -it else it }
                    .let { if (rightMovingForward) it else -it }

    var xyPosition = Position(0.Foot, 0.Foot, 0.Degree)
        private set

    private var leftMovingForward = false
    private var rightMovingForward = false
    private val matrixTracking = RotationMatrixTracking(trackLength, xyPosition)
    //    private val tracking = simpleVectorTracking(trackLength, xyPosition)
    fun accumulateOdometry(ticksL: Int, ticksR: Int) {
        val posL = toLeftPosition(ticksL)
                .let { if (flipOdometryLeft) -it else it }
        val posR = toRightPosition(ticksR)
                .let { if (flipOdometryRight) -it else it }

        xyPosition = matrixTracking(posL, posR)
        leftMovingForward = !posL.isNegative
        rightMovingForward = !posR.isNegative
    }
}
