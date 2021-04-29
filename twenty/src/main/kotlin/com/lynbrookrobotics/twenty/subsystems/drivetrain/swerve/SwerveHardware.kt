package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

import com.kauailabs.navx.frc.AHRS
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve.module.ModuleComponent
import com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve.module.ModuleHardware
import edu.wpi.first.wpilibj.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class SwerveHardware(
) : SubsystemHardware<SwerveHardware, SwerveComponent>(), GenericDriveHardware {
    override val priority = Priority.RealTime
    override val period = 30.milli(Second)
    override val syncThreshold = 5.milli(Second)
    override val name = "SwerveDrive"

    override val conversions = SwerveConversions(this)

    private val driftTolerance by pref(0.2, DegreePerSecond)

    val m1Hardware = ModuleHardware(1,2,3,4)
    val m2Hardware = ModuleHardware(1,2,3,4)
    val m3Hardware = ModuleHardware(1,2,3,4)
    val m4Hardware = ModuleHardware(1,2,3,4)

    val m1Comp = ModuleComponent(m1Hardware)
    val m2Comp = ModuleComponent(m2Hardware)
    val m3Comp = ModuleComponent(m3Hardware)
    val m4Comp = ModuleComponent(m4Hardware)

    val modules = mutableListOf<ModuleComponent>(m1Comp, m2Comp, m3Comp, m4Comp)

    private val gyro by hardw { AHRS(SerialPort.Port.kUSB) }.configure {
        blockUntil() { it.isConnected }
        blockUntil() { !it.isCalibrating }
        it.zeroYaw()
    }.verify("NavX should be connected") {
        it.isConnected
    }.verify("NavX should be finished calibrating on startup") {
        !it.isCalibrating
    }.verify("NavX yaw should not drift after calibration") {
        it.rate.DegreePerSecond in `±`(driftTolerance)
    }

    private val escNamed = Named("ESC Odometry", this)

    override val position = sensor {
        conversions.odometry(
            arrayOf(
                Pair(
                    modules[0].hardware.wheelPosition.optimizedRead(it, syncThreshold).y,
                    modules[0].hardware.anglePosition.optimizedRead(it, syncThreshold).y
                ),
                Pair(
                    modules[1].hardware.wheelPosition.optimizedRead(it, syncThreshold).y,
                    modules[1].hardware.anglePosition.optimizedRead(it, syncThreshold).y
                ),
                Pair(
                    modules[2].hardware.wheelPosition.optimizedRead(it, syncThreshold).y,
                    modules[2].hardware.anglePosition.optimizedRead(it, syncThreshold).y
                ),
                Pair(
                    modules[3].hardware.wheelPosition.optimizedRead(it, syncThreshold).y,
                    modules[3].hardware.anglePosition.optimizedRead(it, syncThreshold).y
                )
            ),
            gyro.yaw.Degree
        ) stampWith it
    }
        .with(graph("X Location", Foot, escNamed)) { it?.x ?: 0.Foot }
        .with(graph("Y Location", Foot, escNamed)) { it?.y ?: 0.Foot }
        .with(graph("Bearing", Degree, escNamed)) { it?.bearing ?: 0.Radian }

    val pitch = sensor {
        gyro.pitch.Degree stampWith it
    }.with(graph("Pitch", Degree))

}