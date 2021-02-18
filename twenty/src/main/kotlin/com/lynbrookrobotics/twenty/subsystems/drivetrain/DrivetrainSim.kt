package com.lynbrookrobotics.twenty.subsystems.drivetrain

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX
import edu.wpi.first.hal.SimDouble
import edu.wpi.first.hal.simulation.SimDeviceDataJNI
import edu.wpi.first.wpilibj.drive.DifferentialDrive
import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveOdometry
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim
import edu.wpi.first.wpilibj.smartdashboard.Field2d
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj.system.plant.DCMotor
import edu.wpi.first.wpiutil.math.VecBuilder
import info.kunalsheth.units.generated.*

object SimConstants {
    const val gearing = 10.4166666667
    const val jKgMetersSquared = 7.5
    const val massKg = 50.0
    const val wheelRadiusMeters = 0.076
    const val trackWidthMeters = 0.61
}

class DrivetrainSim(component: DrivetrainComponent) {

    private val drive = DifferentialDrive(
        component.hardware.leftMasterEsc as WPI_TalonSRX,
        component.hardware.rightMasterEsc as WPI_TalonSRX
    )
    private val driveSim = DifferentialDrivetrainSim(
        DCMotor.getFalcon500(2),
        SimConstants.gearing,
        SimConstants.jKgMetersSquared,
        SimConstants.massKg,
        SimConstants.wheelRadiusMeters,
        SimConstants.trackWidthMeters,
        VecBuilder.fill(0.001, 0.001, 0.001, 0.1, 0.1, 0.005, 0.005),
    )

    private val odometry = DifferentialDriveOdometry(Rotation2d(0.0))

    private val leftDriveEscSim = (component.hardware.leftMasterEsc as WPI_TalonSRX).simCollection
    private val rightDriveEscSim = (component.hardware.rightMasterEsc as WPI_TalonSRX).simCollection

    private val fieldSim = Field2d()

    init {
        SmartDashboard.putData("Field", fieldSim)

        with(component.hardware) {
            leftMasterEsc.inverted = false
            leftMasterEsc.setSensorPhase(false)
            rightMasterEsc.inverted = false
            rightMasterEsc.setSensorPhase(false)

            component.clock.runOnTick {
                println("SetInputs: ${leftMasterEsc.motorOutputVoltage}, ${rightMasterEsc.motorOutputVoltage}")
                driveSim.setInputs(
                    leftMasterEsc.motorOutputVoltage,
                    -rightMasterEsc.motorOutputVoltage,
                )

                driveSim.update(period.Second)

                leftDriveEscSim.setQuadratureRawPosition(
                    conversions.encoder.left.native(driveSim.leftPositionMeters.Metre).toInt()
                )
                leftDriveEscSim.setQuadratureVelocity(
                    conversions.encoder.left.native(driveSim.leftVelocityMetersPerSecond.Metre / Second).toInt()
                )
                rightDriveEscSim.setQuadratureRawPosition(
                    conversions.encoder.right.native(driveSim.rightPositionMeters.Metre).toInt()
                )
                rightDriveEscSim.setQuadratureVelocity(
                    conversions.encoder.right.native(driveSim.rightVelocityMetersPerSecond.Metre / Second).toInt()
                )

                // Update NavX gyro (per
                // https://pdocs.kauailabs.com/navx-mxp/software/roborio-libraries/java/)
                val dev = SimDeviceDataJNI.getSimDeviceHandle("navX-Sensor[0]")
                val angle = SimDouble(SimDeviceDataJNI.getSimValueHandle(dev, "Yaw"))
                angle.set(-driveSim.heading.degrees)

                odometry.update(
                    Rotation2d.fromDegrees(gyro.angle),
                    conversions.encoder.left.realPosition(leftMasterEsc.selectedSensorPosition).Metre,
                    conversions.encoder.right.realPosition(rightMasterEsc.selectedSensorPosition).Metre,
                )
                fieldSim.robotPose = odometry.poseMeters
            }
        }
    }
}