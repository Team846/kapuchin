package com.lynbrookrobotics.kapuchin.routines.teleop

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

suspend fun DrivetrainComponent.teleop(driver: DriverHardware) = startRoutine("teleop") {
    val accelerator by driver.accelerator.readOnTick.withoutStamps
    val steering by driver.steering.readOnTick.withoutStamps
    val gyro by hardware.gyroInput.readEagerly.withStamps

    val turnTargetIntegrator = infiniteIntegrator(::times,
            gyro.x, gyro.y.velocity
    )
    val turnControl = pidControlLoop(::div, ::times, turningPositionGains) {
        val steeringForwardBlend =
                if (steering == 0.Percent) 0.Percent
                else steering.abs / (steering.abs + accelerator.abs)
        turnTargetIntegrator(it, maxTurningSpeed * steering * steeringForwardBlend)
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

suspend fun DrivetrainComponent.pointWithLimelight(limelight: LimelightSystem) = startRoutine("point with limelight") {
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
        val right = -left

        hardware.offloadedSettings.run {
            TwoSided(
                    VelocityOutput(native(leftVelocityGains), native(left)),
                    VelocityOutput(native(rightVelocityGains), native(right))
            )
        }
    }
}

suspend fun DrivetrainComponent.driveWithLimelight(limelight: LimelightSystem) = startRoutine("drive with limelight") {
    val limelightAngle by limelight.angleToTarget.readOnTick.withoutStamps
    val limelightDistance by limelight.distanceToTarget.readOnTick.withoutStamps
    val gyro by hardware.gyroInput.readOnTick.withStamps

    val startingGyroAngle = gyro.y.angle
    val startingLimelightAngle = limelightAngle
    val startingLimelightDistance = limelightDistance

    val bearing = when (startingLimelightAngle) {
        null -> gyro.y.angle
        else -> startingLimelightAngle - startingGyroAngle
    }

    val radius = startingLimelightDistance / (2 * Math.sin(bearing / 2))

    DrivetrainComponent.arcTo(bearing, radius, 2.Degree, 0.25.Foot, )

}


suspend fun DrivetrainComponent.arcTo(
        bearing: Angle, radius: Length,
        angleTolerance: Angle, distanceTolerance: Length,

        acceleration: Acceleration,
        topSpeed: Velocity,
        deceleration: Acceleration = acceleration,
        endingSpeed: Velocity = 0.FootPerSecond,
        kickstart: Velocity = 3.Inch / Second
) = startRoutine("arc") {
    val position by hardware.position.readOnTick.withoutStamps
    val velocity by hardware.velocity.readOnTick.withoutStamps
    val gyro by hardware.gyroInput.readEagerly.withStamps

    // s = r × θ
    val theta = bearing - gyro.y.angle
    val rL = radius + trackSize / 2
    val rR = radius - trackSize / 2
    val sL = rL * theta / Radian
    val sR = rR * theta / Radian

    val rSmall = rL minMag rR
    val rBig = rL maxMag rR
    val rSmallBigRatio = rSmall / rBig

    val profile = trapezoidalMotionProfile(
            distance = sL maxMag sR,
            startingSpeed = kickstart maxMag velocity.avg,
            acceleration = acceleration,
            topSpeed = topSpeed,
            deceleration = deceleration,
            endingSpeed = endingSpeed
    )

    val startingPostion = position
    val turnControl = pidControlLoop(::div, ::times, turningPositionGains) {
        // θ = s ÷ r
        (position.avg - startingPostion.avg) / radius * Radian
    }

    val slRange = sL `±` distanceTolerance
    val srRange = sR `±` distanceTolerance
    val bearingRange = bearing `±` angleTolerance

    controller {
        if (
                position.left in slRange &&
                position.right in srRange &&
                gyro.y.angle in bearingRange
        ) null
        else {
            val turn = turnControl(gyro.x, gyro.y.angle)

            val dx = position - startingPostion
            val bigTarget = profile(if (rBig == rL) dx.left else dx.right)

            // s₂ = s₁ × (r₂ ÷ r₁)
            // v₂ = v₁ × (r₂ ÷ r₁)
            // a₂ = a₁ × (r₂ ÷ r₁)
            val left = (if (rBig == rL) bigTarget else bigTarget * rSmallBigRatio) + turn
            val right = (if (rBig == rR) bigTarget else bigTarget * rSmallBigRatio) - turn

            hardware.offloadedSettings.run {
                TwoSided(
                        VelocityOutput(native(leftVelocityGains), native(left)),
                        VelocityOutput(native(rightVelocityGains), native(right))
                )
            }
        }
    }
}

suspend fun DrivetrainComponent.driveStraight(
        distance: Length, bearing: Angle,
        distanceTolerance: Length, angleTolerance: Angle,

        acceleration: Acceleration,
        topSpeed: Velocity,
        deceleration: Acceleration = acceleration,
        endingSpeed: Velocity = 0.FootPerSecond,
        kickstart: Velocity = 3.Inch / Second
) = startRoutine("straight") {
    val position by hardware.position.readOnTick.withoutStamps
    val velocity by hardware.velocity.readOnTick.withoutStamps
    val gyro by hardware.gyroInput.readEagerly.withStamps

    val profile = trapezoidalMotionProfile(
            distance = distance,
            startingSpeed = kickstart maxMag velocity.avg,
            acceleration = acceleration,
            topSpeed = topSpeed,
            deceleration = deceleration,
            endingSpeed = endingSpeed
    )

    val startingPostion = position
    val turnControl = pidControlLoop(::div, ::times, turningPositionGains) { bearing }

    val distanceRange = distance `±` distanceTolerance
    val bearingRange = bearing `±` angleTolerance

    controller {
        if (
                position.left in distanceRange &&
                position.right in distanceRange &&
                gyro.y.angle in bearingRange
        ) null
        else {
            val turn = turnControl(gyro.x, gyro.y.angle)

            val forward = profile((position - startingPostion).avg)
            val left = forward + turn
            val right = forward - turn

            hardware.offloadedSettings.run {
                TwoSided(
                        VelocityOutput(native(leftVelocityGains), native(left)),
                        VelocityOutput(native(rightVelocityGains), native(right))
                )
            }
        }
    }
}