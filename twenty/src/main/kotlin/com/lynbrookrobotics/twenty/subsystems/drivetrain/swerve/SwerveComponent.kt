package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import info.kunalsheth.units.generated.*

class SwerveComponent(hardware: SwerveHardware
) :
    Component<SwerveComponent, SwerveHardware, FourSided<OffloadedOutput>>(hardware),
    GenericDriveComponent {

    val maxFrontLeftSpeed by pref(11.9, FootPerSecond)
    val maxFrontRightSpeed by pref(12.5, FootPerSecond)
    val maxBackRightSpeed by pref(12.5, FootPerSecond)
    val maxBackLeftSpeed by pref(12.5, FootPerSecond)
    val maxAcceleration by pref(10, FootPerSecondSquared)
    val percentMaxOmega by pref(75, Percent)

    val speedFactor by pref(50, Percent)
    val constantSpeed by pref(5, FootPerSecond)
    val maxExtrapolate by pref(40, Inch)

    override val maxSpeed get() = maxFrontLeftSpeed min maxFrontRightSpeed min maxBackRightSpeed min maxBackLeftSpeed
    val maxOmega get() = maxSpeed / hardware.conversions.trackLength / 2 * Radian

    val velocityGains by pref {
        val kP by pref(5, Volt, 2, FootPerSecond)
        val kF by pref(110, Percent)
        ({
            val TR = OffloadedEscGains(
                kP = hardware.conversions.encoder.frontRight.native(kP),
                kF = hardware.conversions.encoder.frontRight.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxFrontLeftSpeed)
                ) * kF.Each
            )
            val TL = OffloadedEscGains(
                kP = hardware.conversions.encoder.frontLeft.native(kP),
                kF = hardware.conversions.encoder.frontLeft.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxFrontRightSpeed)
                ) * kF.Each
            )
            val BR = OffloadedEscGains(
                kP = hardware.conversions.encoder.backRight.native(kP),
                kF = hardware.conversions.encoder.backRight.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxBackRightSpeed)
                ) * kF.Each
            )
            val BL = OffloadedEscGains(
                kP = hardware.conversions.encoder.backLeft.native(kP),
                kF = hardware.conversions.encoder.backLeft.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxBackLeftSpeed)
                ) * kF.Each
            )

            FourSided(TR, TL, BR, BL)

        })
    }
    private val bearingGainsNamed = Named("bearingGains", this)
    override val bearingKp by bearingGainsNamed.pref(5, FootPerSecond, 45, Degree)
    override val bearingKd by bearingGainsNamed.pref(3, FootPerSecond, 360, DegreePerSecond)

    override val fallbackController: SwerveComponent.(Time) -> FourSided<OffloadedOutput> = {
        FourSided(PercentOutput(hardware.escConfig, 0.Percent))
    }

    private val frontLeftEscOutputGraph = graph("Front Left ESC Output", Volt)
    private val frontRightEscOutputGraph = graph("Front Right ESC Output", Volt)
    private val backLeftEscOutputGraph = graph("Back Left ESC Output", Volt)
    private val backRightEscOutputGraph = graph("Back Right ESC Output", Volt)

    private val frontLeftEscErrorGraph = graph("Front Left ESC Error", Each)
    private val frontRightEscErrorGraph = graph("Front Right ESC Error", Each)
    private val backLeftEscErrorGraph = graph("Back Left ESC Error", Each)
    private val backRightEscErrorGraph = graph("Back Right ESC Error", Each)

    override fun SwerveHardware.output(value: FourSided<OffloadedOutput>) {
        TODO("Not implemented")
    }

}