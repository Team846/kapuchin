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
    private val flipOdometryLeft by pref(false)
    private val flipOdometryRight by pref(true)
    private val rightTrim by pref(1.00621994)
    private val trackLength by pref(2.05, Foot)

    val nativeEncoderCountMultiplier by pref(4)

    val encoder by pref {
        val encoderGear by pref(22)
        val wheelGear by pref(72)
        val resolution by pref(1000)
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
                            toLeftPosition(resolution, enc),
                            toRightPosition(resolution, enc)
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

    private val matrixCache = (-8..8)
            .flatMap {
                setOf(
                        theta(toLeftPosition(it), 0.Foot, trackLength),
                        theta(0.Foot, toRightPosition(it), trackLength)
                )
            }
            .map { it to RotationMatrix(it) }
            .toMap()

    val matrixTracking = RotationMatrixTracking(trackLength, Position(0.Foot, 0.Foot, 0.Degree), matrixCache)

    fun accumulateOdometry(ticksL: Int, ticksR: Int) {
        val posL = toLeftPosition(ticksL)
                .let { if (flipOdometryLeft) -it else it }
        val posR = toRightPosition(ticksR)
                .let { if (flipOdometryRight) -it else it }

        matrixTracking(posL, posR)
    }
}
