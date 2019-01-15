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
    val gyro by hardware.gyroInput.readEagerly.withStamps

    val startingGyroAngle = gyro
    val starting

    val turnControl = pidControlLoop(::div, ::times, turningPositionGains) {

    }

    controller {
        val forwardVelocity = topSpeed * accelerator
        val steeringVelocity = topSpeed * steering //+ turnControl(gyro.x, gyro.y.angle)

        val left = forwardVelocity + steeringVelocity
        val right = forwardVelocity - steeringVelocity

        hardware.offloadedSettings.run {
            TwoSided(
                    VelocityOutput(native(leftVelocityGains), native(left)),
                    VelocityOutput(native(rightVelocityGains), native(right))
            )
        }
    }
}