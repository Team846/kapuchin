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

    override val trackLength by pref(2, Foot)
    override val radius by pref(2, Foot)

    private val frontLeftTrim by pref(1.0)
    private val frontRightTrim by pref(1.0)
    private val backRightTrim by pref(1.0)
    private val backLeftTrim by pref(1.0)

    private val wheelRadius by pref {
        val frontLeft by pref(3, Inch)
        val frontRight by pref(3, Inch)
        val backLeft by pref(3, Inch)
        val backRight by pref(3, Inch)
        ({ FourSided(frontRight, frontLeft, backRight, backLeft) })
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

            val frontLeft = LinearOffloadedNativeConversion(
                ::p, ::p, ::p, ::p,
                nativeOutputUnits = 1023, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                nativeFeedbackUnits = nativeResolution,
                perFeedbackQuantity = wheelRadius.frontLeft * enc.angle(nativeResolution) * frontLeftTrim / Radian,
                nativeTimeUnit = 100.milli(Second), nativeRateUnit = 1.Second
            )
            val frontRight = frontLeft.copy(
                perFeedbackQuantity = wheelRadius.frontRight * enc.angle(nativeResolution) * frontRightTrim / Radian
            )
            val backRight = frontLeft.copy(
                perFeedbackQuantity = wheelRadius.backRight * enc.angle(nativeResolution) * backRightTrim / Radian
            )
            val backLeft = frontLeft.copy(
                perFeedbackQuantity = wheelRadius.backLeft * enc.angle(nativeResolution) * backLeftTrim / Radian
            )
            FourSided(frontRight, frontLeft, backRight, backLeft)
        })
    }

    private var noTicksFR = true
    private var noTicksFL = true
    private var noTicksBR = true
    private var noTicksBL = true

    private var lastFrontLeft = 0.Foot
    private var lastFrontRight = 0.Foot
    private var lastBackLeft = 0.Foot
    private var lastBackRight = 0.Foot

    val tracking = SwerveOdometry(Position(0.Foot, 0.Foot, 0.Radians), radius, trackLength)

    fun odometry(modules: Array<Pair<Length, Angle>>){
        if(noTicksFR && modules[0].first != 0.Foot) log(Level.Debug) {
            "Received first front right tick at ${currentTime withDecimals 2}"
        }.also { noTicksFR = false }
        if(noTicksFL && modules[1].first != 0.Foot) log(Level.Debug) {
            "Received first front left tick at ${currentTime withDecimals 2}"
        }.also { noTicksBR = false }
        if(noTicksFR && modules[2].first != 0.Foot) log(Level.Debug) {
            "Received first back right tick at ${currentTime withDecimals 2}"
        }.also { noTicksBL = false }
        if(noTicksFR && modules[3].first != 0.Foot) log(Level.Debug) {
            "Received first back left tick at ${currentTime withDecimals 2}"
        }.also { noTicksFR = false }

        tracking.updatePosition(modules)

        lastFrontRight = modules[0].first
        lastFrontLeft = modules[1].first
        lastBackRight = modules[2].first
        lastBackLeft = modules[3].first
    }
}