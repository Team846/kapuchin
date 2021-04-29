package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

import com.ctre.phoenix.motorcontrol.FeedbackDevice.IntegratedSensor
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.TalonFX
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
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainConversions
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

    override val position = sensor {
        conversions.odometry(
            leftPosition.optimizedRead(it, syncThreshold).y,
            rightPosition.optimizedRead(it, syncThreshold).y,
            gyro.yaw.Degree
        )
        conversions.tracking.run { Position(x, y, bearing) } stampWith it
    }
        .with(graph("X Location", Foot, escNamed)) { it.x }
        .with(graph("Y Location", Foot, escNamed)) { it.y }
        .with(graph("Bearing", Degree, escNamed)) { it.bearing }

    val pitch = sensor {
        gyro.pitch.Degree stampWith it
    }.with(graph("Pitch", Degree))

}