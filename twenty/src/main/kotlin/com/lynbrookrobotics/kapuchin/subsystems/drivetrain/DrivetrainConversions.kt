package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class DrivetrainConversions(val hardware: DrivetrainHardware) :
        Named by Named("Conversions", hardware),
        GenericDrivetrainConversions {
    private val wheelRadius by pref(3, Inch)

    private val leftTrim by pref(1.0)
    private val rightTrim by pref(1.0)
    override val trackLength by pref(2, Foot)

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

            val nat = LinearOffloadedNativeConversion(::p, ::p, ::p, ::p,
                    nativeOutputUnits = 1023, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
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

    val t2sOdometry = TicksToSerialOdometry(this)

    class TicksToSerialOdometry(val conversions: DrivetrainConversions) : Named by Named("T2S Odometry", conversions) {
        private var noTicksL = true
        private var noTicksR = true
        val matrixTracking = RotationMatrixTracking(conversions.trackLength, Position(0.Foot, 0.Foot, 0.Degree), conversions.matrixCache)

        val flipOdometrySides by pref(true)
        val flipLeftOdometry by pref(true)
        val flipRightOdometry by pref(false)
        operator fun invoke(deltaT2sTicksL: Int, deltaT2sTicksR: Int) {
            if (noTicksL && deltaT2sTicksL != 0) log(Level.Debug) {
                "Received first left tick at $currentTime"
            }.also { noTicksL = false }

            if (noTicksR && deltaT2sTicksR != 0) log(Level.Debug) {
                "Received first right tick at $currentTime"
            }.also { noTicksR = false }

            val fl = if (flipLeftOdometry) -deltaT2sTicksL else deltaT2sTicksL
            val fr = if (flipRightOdometry) -deltaT2sTicksR else deltaT2sTicksR
            val l = if (flipOdometrySides) fr else fl
            val r = if (flipOdometrySides) fl else fr

            matrixTracking(
                    conversions.toLeftPosition(l),
                    conversions.toRightPosition(r)
            )
        }
    }

    val escOdometry = EscOdometry(this)

    class EscOdometry(val conversions: DrivetrainConversions) : Named by Named("ESC Odometry", conversions) {
        private var noTicksL = true
        private var noTicksR = true
        val tracking = SimpleVectorTracking(conversions.trackLength, Position(0.Foot, 0.Foot, 0.Degree))

        private var lastL = 0
        private var lastR = 0
        operator fun invoke(totalEscTicksL: Int, totalEscTicksR: Int) = conversions.run {
            if (noTicksL && totalEscTicksL != 0) log(Level.Debug) {
                "Received first left tick at $currentTime"
            }.also { noTicksL = false }

            if (noTicksR && totalEscTicksR != 0) log(Level.Debug) {
                "Received first right tick at $currentTime"
            }.also { noTicksR = false }

            tracking(
                    toLeftPosition(
                            (totalEscTicksL - lastL) / nativeEncoderCountMultiplier
                    ),
                    toRightPosition(
                            (totalEscTicksR - lastR) / nativeEncoderCountMultiplier
                    )
            )
            lastL = totalEscTicksL
            lastR = totalEscTicksR
        }
    }
}
