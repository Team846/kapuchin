package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class SwerveConversions(val hardware: SwerveHardware) : Named by Named("Conversions", hardware),
    GenericDriveConversions {
    override val trackLength: Length by pref (2, Foot)
    override val botRadius: Length by pref (1, Foot)

    private val fRTrim by pref(1.0)
    private val fLTrim by pref(1.0)
    private val bRTrim by pref(1.0)
    private val bLTrim by pref(1.0)


    private val wheelRadius by pref {
        val fR by pref(3, Inch)
        val fL by pref(3, Inch)
        val bR by pref(3, Inch)
        val bL by pref(3, Inch)
        ({ FourSided(fR, fL, bR, bL) })
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

            val frontRight = LinearOffloadedNativeConversion(
                ::p, ::p, ::p, ::p,
                nativeOutputUnits = 1023, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                nativeFeedbackUnits = nativeResolution,
                perFeedbackQuantity = wheelRadius.frontRight * enc.angle(nativeResolution) * fRTrim / Radian,
                nativeTimeUnit = 100.milli(Second), nativeRateUnit = 1.Second
            )
            val frontLeft = frontRight.copy(
                perFeedbackQuantity = wheelRadius.frontLeft * enc.angle(nativeResolution) * fLTrim / Radian
            )
            val backRight = frontRight.copy(
                perFeedbackQuantity = wheelRadius.backRight * enc.angle(nativeResolution) * bRTrim / Radian
            )
            val backLeft = frontRight.copy(
                perFeedbackQuantity = wheelRadius.backLeft * enc.angle(nativeResolution) * bLTrim / Radian
            )
            FourSided(frontRight, frontLeft, backRight, backLeft)
        })
    }

    val tracking = SwerveOdometry(Position(0.Foot,0.Foot,0.Degree), botRadius, trackLength)

    private var noTicksFL = true
    private var noTicksFR = true
    private var noTicksBR = true
    private var noTicksBL = true
    private var lastFrontLeft = 0.Foot
    private var lastFrontRight = 0.Foot
    private var lastBackLeft = 0.Foot
    private var lastBackRight = 0.Foot

    fun odometry(modules: Array<Pair<Length, Angle>>, bearing: Angle) {
        //odom.updatePosition(wheelDist)
        if (noTicksFL && modules[0].first != 0.Foot) log(Level.Debug) {
            "Received first top left tick at ${currentTime withDecimals 2}"
        }.also { noTicksFL = false }

        if (noTicksFR && modules[1].first != 0.Foot) log(Level.Debug) {
            "Received first top right tick at ${currentTime withDecimals 2}"
        }.also { noTicksFR = false }

        if (noTicksBR && modules[2].first != 0.Foot) log(Level.Debug) {
            "Received first bottom right tick at ${currentTime withDecimals 2}"
        }.also { noTicksBR = false }

        if (noTicksBL && modules[3].first != 0.Foot) log(Level.Debug) {
            "Received first bottom left tick at ${currentTime withDecimals 2}"
        }.also { noTicksBL = false }

        tracking.updatePosition(modules)
    }
}