package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.kapuchin.timing.monitoring.RealtimeChecker.Companion.realtimeChecker
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainHardware
import info.kunalsheth.units.generated.*

class SwerveComponent(hardware: SwerveHardware) :
    Component<SwerveComponent, SwerveHardware, FourSided<OffloadedOutput>>(hardware),
    GenericDriveComponent { 
    
    val maxFrontRightSpeed by pref(10, FootPerSecond)
    val maxFrontLeftSpeed by pref(10,FootPerSecond)
    val maxBackRightSpeed by pref(10, FootPerSecond)
    val maxBackLeftSpeed by pref(10, FootPerSecond)
    
    val maxAcceleration by pref(6, FootPerSecondSquared)
    val percentMaxOmega by pref(75, Percent)

    val speedFactor by pref(50, Percent)
    val constantSpeed by pref(5, FootPerSecond)

    override val maxSpeed: Velocity
        get() = maxFrontRightSpeed min maxFrontLeftSpeed min maxBackRightSpeed min maxBackLeftSpeed


    val velocityGains by pref {
        val kP by pref(5, Volt, 2, FootPerSecond)
        val kF by pref(110, Percent)
        ({
            val frontRight = OffloadedEscGains(
                kP = hardware.conversions.encoder.frontRight.native(kP),
                kF = hardware.conversions.encoder.frontRight.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxFrontRightSpeed)
                ) * kF.Each
            )
            val frontLeft = OffloadedEscGains(
                kP = hardware.conversions.encoder.frontLeft.native(kP),
                kF = hardware.conversions.encoder.frontLeft.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxFrontLeftSpeed)
                ) * kF.Each
            )
            val backRight = OffloadedEscGains(
                kP = hardware.conversions.encoder.backRight.native(kP),
                kF = hardware.conversions.encoder.backRight.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxBackRightSpeed)
                ) * kF.Each
            )
            val backLeft = OffloadedEscGains(
                kP = hardware.conversions.encoder.backLeft.native(kP),
                kF = hardware.conversions.encoder.backLeft.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxBackLeftSpeed)
                ) * kF.Each
            )
            FourSided(frontRight, frontLeft, backRight, backLeft)
        })
    }

    private val bearingGainsNamed = Named("bearingGains", this)

    override val bearingKp by bearingGainsNamed.pref(5, FootPerSecond, 45, Degree)
    override val bearingKd by bearingGainsNamed.pref(3, FootPerSecond, 360, DegreePerSecond)

    override val fallbackController: SwerveComponent.(Time) -> FourSided<OffloadedOutput> = {
        FourSided(PercentOutput(hardware.escConfig, 0.Percent))
    }

    private val frontRightEscOutputGraph = graph("Front Right ESC Output", Volt)
    private val frontLeftEscOutputGraph = graph("Front Left ESC Output", Volt)
    private val backRightEscOutputGraph = graph("Back Right ESC Output", Volt)
    private val backLeftEscOutputGraph = graph("Back Left ESC Output", Volt)

    private val frontRightEscErrorGraph = graph("Front Right ESC Error", Each)
    private val frontLeftEscErrorGraph = graph("Front Left ESC Error", Each)
    private val backRightEscErrorGraph = graph("Back Right ESC Error", Each)
    private val backLeftEscErrorGraph = graph("Back Left ESC Error", Each)

    override fun DrivetrainHardware.output(value: FourSided<OffloadedOutput>) {
        value.frontLeft.writeTo(frontLeftEsc)
        value.frontRight.writeTo(frontRightEsc)
        value.backLeft.writeTo(backLeftEsc)
        value.backRight.writeTo(backRightEsc)

        frontRightEscOutputGraph(currentTime, frontRightEsc.motorOutputVoltage.Volt)
        frontLeftEscOutputGraph(currentTime, frontLeftEsc.motorOutputVoltage.Volt)
        backRightEscOutputGraph(currentTime, backRightEsc.motorOutputVoltage.Volt)
        backLeftEscOutputGraph(currentTime, backLeftEsc.motorOutputVoltage.Volt)

        frontRightEscErrorGraph(currentTime, frontRightEsc.closedLoopError.Each)
        frontLeftEscErrorGraph(currentTime, frontLeftEsc.closedLoopError.Each)
        backRightEscErrorGraph(currentTime, backRightEsc.closedLoopError.Each)
        backLeftEscErrorGraph(currentTime, backLeftEsc.closedLoopError.Each)
    }

    init {
        if (clock is Ticker) clock.realtimeChecker(hardware.jitterPulsePin::set) { hardware.jitterReadPin.period.Second }
    }

}