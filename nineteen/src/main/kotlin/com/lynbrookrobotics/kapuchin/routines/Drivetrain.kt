package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.TwoSided
import com.lynbrookrobotics.kapuchin.control.data.avg
import com.lynbrookrobotics.kapuchin.control.data.minus
import com.lynbrookrobotics.kapuchin.control.electrical.rampRateLimiter
import com.lynbrookrobotics.kapuchin.control.loops.pid.pidControlLoop
import com.lynbrookrobotics.kapuchin.control.math.infiniteIntegrator
import com.lynbrookrobotics.kapuchin.control.math.kinematics.trapezoidalMotionProfile
import com.lynbrookrobotics.kapuchin.control.maxMag
import com.lynbrookrobotics.kapuchin.control.minMag
import com.lynbrookrobotics.kapuchin.hardware.LimelightSystem
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VelocityOutput
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.`±`

suspend fun DrivetrainComponent.pointWithLimelight(driver: DriverHardware, limelight: LimelightSystem) = startRoutine("point with limelight") {
    val gyro by hardware.gyroInput.readOnTick.withStamps
    val limelightAngle by limelight.angleToTarget.readOnTick.withoutStamps

    val startingGyroAngle = gyro.y.angle
    val startingLimelightAngle = limelightAngle

    val turnControl = pidControlLoop(::div, ::times, turningPositionGains) {
        if(startingLimelightAngle == null) gyro.y.angle
        else startingLimelightAngle - startingGyroAngle
    }

    controller {
        val left = turnControl(gyro.x, gyro.y.angle)
        val right = -turnControl(gyro.x, gyro.y.angle)

        hardware.offloadedSettings.run {
            TwoSided(
                    VelocityOutput(native(leftVelocityGains), native(left)),
                    VelocityOutput(native(rightVelocityGains), native(right))
            )
        }
    }
}