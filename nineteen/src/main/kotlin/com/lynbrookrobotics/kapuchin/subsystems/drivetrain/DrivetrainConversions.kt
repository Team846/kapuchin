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

    private val leftTrim by pref(1.0)
    private val rightTrim by pref(1.0)
    val trackLength by pref(2, Foot)

    val nativeEncoderCountMultiplier by pref(4)

    val encoder by pref {
        val encoderGear by pref(22)
        val wheelGear by pref(72)
        val resolution by pref(256)
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
                            toLeftPosition(resolution, enc).abs,
                            toRightPosition(resolution, enc).abs
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

    fun toRightPosition(
            ticks: Int, conv: EncoderConversion = encoderConversion
    ): Length = wheelRadius * conv.angle(ticks.toDouble()) * rightTrim / Radian

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

    val flipOdometrySides by pref(false)
    val flipLeftOdometry by pref(false)
    val flipRightOdometry by pref(true)
    fun accumulateOdometry(ticksL: Int, ticksR: Int) {
        val fl = if(flipLeftOdometry) -ticksL else ticksL
        val fr = if(flipRightOdometry) -ticksR else ticksR
        val l = if(flipOdometrySides) fr else fl
        val r = if(flipOdometrySides) fl else fr

        matrixTracking(
                toLeftPosition(l),
                toRightPosition(r)
        )
    }
}
