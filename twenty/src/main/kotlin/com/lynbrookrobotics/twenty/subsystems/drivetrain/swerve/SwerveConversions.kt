package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

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

class SwerveConversions(val hardware: SwerveHardware) : Named by Named("Conversions", hardware),
    GenericDriveConversions {
    override val trackLength: Length by pref (2, Foot)
    override val botRadius: Length by pref (1, Foot)

    private val TRTrim by pref(1.0)
    private val TLTrim by pref(1.0)
    private val BRTrim by pref(1.0)
    private val BLTrim by pref(1.0)


    private val wheelRadius by pref {
        val TR by pref(3, Inch)
        val TL by pref(3, Inch)
        val BR by pref(3, Inch)
        val BL by pref(3, Inch)
        ({ FourSided(TR, TL, BR, BL) })
    }

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

            val topRight = LinearOffloadedNativeConversion(
                ::p, ::p, ::p, ::p,
                nativeOutputUnits = 1023, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                nativeFeedbackUnits = nativeResolution,
                perFeedbackQuantity = wheelRadius.TR * enc.angle(nativeResolution) * TRTrim / Radian,
                nativeTimeUnit = 100.milli(Second), nativeRateUnit = 1.Second
            )
            val topLeft = topRight.copy(
                perFeedbackQuantity = wheelRadius.TL * enc.angle(nativeResolution) * TLTrim / Radian
            )
            val bottomRight = topRight.copy(
                perFeedbackQuantity = wheelRadius.BR * enc.angle(nativeResolution) * BRTrim / Radian
            )
            val bottomLeft = topRight.copy(
                perFeedbackQuantity = wheelRadius.BL * enc.angle(nativeResolution) * BLTrim / Radian
            )
            FourSided(topRight, topLeft, bottomRight, bottomLeft)
        })
    }

    val tracking = SwerveOdometry(Position(0.Foot,0.Foot,0.Degree), botRadius, trackLength)

    private var noTicksTL = true
    private var noTicksTR = true
    private var noTicksBR = true
    private var noTicksBL = true
    private var lastTopLeft = 0.Foot
    private var lastTopRight = 0.Foot
    private var lastBottomRight = 0.Foot
    private var lastBottomRight = 0.Foot

    fun odometry(modules: Array<Pair<Length, Angle>>, bearing: Angle) {
        //odom.updatePosition(wheelDist)
        if (noTicksTL && modules[0].first != 0.Foot) log(Level.Debug) {
            "Received first top left tick at ${currentTime withDecimals 2}"
        }.also { noTicksTL = false }

        if (noTicksTR && modules[1].first != 0.Foot) log(Level.Debug) {
            "Received first top right tick at ${currentTime withDecimals 2}"
        }.also { noTicksTR = false }

        if (noTicksBR && modules[2].first != 0.Foot) log(Level.Debug) {
            "Received first bottom right tick at ${currentTime withDecimals 2}"
        }.also { noTicksBR = false }

        if (noTicksBL && modules[3].first != 0.Foot) log(Level.Debug) {
            "Received first bottom left tick at ${currentTime withDecimals 2}"
        }.also { noTicksBL = false }

        tracking.updatePosition(modules)
    }
}