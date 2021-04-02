package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainHardware
import info.kunalsheth.units.generated.*

class SwerveComponent(hardware: SwerveHardware
) :
    Component<SwerveComponent, SwerveHardware, FourSided<OffloadedOutput>>(hardware),
    GenericDriveComponent {

    val maxTopLeftSpeed by pref(11.9, FootPerSecond)
    val maxTopRightSpeed by pref(12.5, FootPerSecond)
    val maxBottomRightSpeed by pref(12.5, FootPerSecond)
    val maxBottomLeftSpeed by pref(12.5, FootPerSecond)
    val maxAcceleration by pref(10, FootPerSecondSquared)
    val percentMaxOmega by pref(75, Percent)

    val speedFactor by pref(50, Percent)
    val constantSpeed by pref(5, FootPerSecond)
    val maxExtrapolate by pref(40, Inch)

    override val maxSpeed get() = maxTopLeftSpeed min maxTopRightSpeed min maxBottomRightSpeed min maxBottomLeftSpeed
    val maxOmega get() = maxSpeed / hardware.conversions.trackLength / 2 * Radian

    val velocityGains by pref {
        val kP by pref(5, Volt, 2, FootPerSecond)
        val kF by pref(110, Percent)
        ({
            val TR = OffloadedEscGains(
                kP = hardware.conversions.encoder.topRight.native(kP),
                kF = hardware.conversions.encoder.topRight.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxTopLeftSpeed)
                ) * kF.Each
            )
            val TL = OffloadedEscGains(
                kP = hardware.conversions.encoder.topLeft.native(kP),
                kF = hardware.conversions.encoder.topLeft.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxTopRightSpeed)
                ) * kF.Each
            )
            val BR = OffloadedEscGains(
                kP = hardware.conversions.encoder.bottomRight.native(kP),
                kF = hardware.conversions.encoder.bottomRight.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxBottomRightSpeed)
                ) * kF.Each
            )
            val BL = OffloadedEscGains(
                kP = hardware.conversions.encoder.bottomLeft.native(kP),
                kF = hardware.conversions.encoder.bottomLeft.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxBottomLeftSpeed)
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

    private val topLeftEscOutputGraph = graph("Top Left ESC Output", Volt)
    private val topRightEscOutputGraph = graph("Top Right ESC Output", Volt)
    private val bottomLeftEscOutputGraph = graph("Bottom Left ESC Output", Volt)
    private val bottomRightEscOutputGraph = graph("Bottom Right ESC Output", Volt)

    private val topLeftEscErrorGraph = graph("Top Left ESC Error", Each)
    private val topRightEscErrorGraph = graph("Top Right ESC Error", Each)
    private val bottomLeftEscErrorGraph = graph("Bottom Left ESC Error", Each)
    private val bottomRightEscErrorGraph = graph("Bottom Right ESC Error", Each)

    override fun SwerveHardware.output(value: FourSided<OffloadedOutput>) {
        TODO("Not implemented")
    }

}