package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.subsystems.*
import com.lynbrookrobotics.twenty.subsystems.driver.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.*
import info.kunalsheth.units.generated.*

suspend fun DrivetrainComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { TwoSided(PercentOutput(hardware.escConfig, target)) }
}

suspend fun DrivetrainComponent.teleop(driver: DriverHardware) = startRoutine("Teleop") {
    val accelerator by driver.accelerator.readOnTick.withoutStamps
    val steering by driver.steering.readOnTick.withoutStamps
    val absSteering by driver.absSteering.readOnTick.withoutStamps

    val position by hardware.position.readOnTick.withStamps

    val uni = UnicycleDrive(this@teleop, this@startRoutine)

    val speedL by hardware.leftSpeed.readOnTick.withoutStamps
    val speedR by hardware.rightSpeed.readOnTick.withoutStamps

    var startingAngle = -absSteering + position.y.bearing

    var lastGc = 0.Second
    controller {
        lastGc = if (
            speedL.isZero && speedR.isZero && accelerator.isZero && steering.isZero &&
            currentTime - lastGc > 2.Second
        ) {
            System.gc()
            currentTime
        } else lastGc

        // https://www.desmos.com/calculator/qkczjursq7
        val cappedAccelerator = accelerator cap `±`(100.Percent - steering.abs)


        val forwardVelocity = maxSpeed * cappedAccelerator
        val steeringVelocity = maxSpeed * steering

        if (!steering.isZero) startingAngle = -absSteering + position.y.bearing

        val (target, _) = uni.speedTargetAngleTarget(forwardVelocity, absSteering + startingAngle)

        val nativeL = hardware.conversions.encoder.left.native(
            target.left + steeringVelocity
        )
        val nativeR = hardware.conversions.encoder.right.native(
            target.right - steeringVelocity
        )

        TwoSided(
            VelocityOutput(hardware.escConfig, velocityGains.left, nativeL),
            VelocityOutput(hardware.escConfig, velocityGains.right, nativeR)
        )
    }
}

suspend fun DrivetrainComponent.turn(target: Angle, tolerance: Angle) = startRoutine("Turn") {
    val uni = UnicycleDrive(this@turn, this@startRoutine)

    controller {
        val (targVels, error) = uni.speedTargetAngleTarget(0.FootPerSecond, target)

        val nativeL = hardware.conversions.encoder.left.native(targVels.left)
        val nativeR = hardware.conversions.encoder.right.native(targVels.right)

        TwoSided(
            VelocityOutput(hardware.escConfig, velocityGains.left, nativeL),
            VelocityOutput(hardware.escConfig, velocityGains.right, nativeR)
        ).takeUnless {
            error.abs < tolerance
        }
    }
}

suspend fun DrivetrainComponent.followTrajectory(
    trajectory: Trajectory,
    maxExtrapolate: Length,
    safetyTolerance: Length = 3.Foot,
//    speedFactor: Dimensionless,
    reverse: Boolean,
    origin: Position = hardware.position.optimizedRead(currentTime, 0.Second).y
) = startRoutine("Follow Trajectory") {

    val follower = TrajectoryFollower(
        this@startRoutine,
        trajectory,
        origin,
        this@followTrajectory,
        maxExtrapolate,
        safetyTolerance,
//        speedFactor,
        reverse
    )

    controller {
        val velocities = follower()
        if (velocities != null) {
            val nativeL = hardware.conversions.encoder.left.native(velocities.left)
            val nativeR = hardware.conversions.encoder.right.native(velocities.right)
            TwoSided(
                VelocityOutput(hardware.escConfig, velocityGains.left, nativeL),
                VelocityOutput(hardware.escConfig, velocityGains.right, nativeR)
            )
        } else {
            null
        }
    }
}

suspend fun DrivetrainComponent.waypoint(
    motionProfile: (Length) -> Velocity,
    target: Waypoint,
    tolerance: Length
) = startRoutine("Waypoint") {
    val position by hardware.position.readOnTick.withStamps
    val uni = UnicycleDrive(this@waypoint, this@startRoutine)

    val waypointDistance = graph("Distance to Waypoint", Foot)

    controller { t ->
        val (_, p) = position
        val location = p.vector

        val distance = distance(location, target).also { waypointDistance(t, it) }

        val targetA = (target - location).bearing
        val speed = motionProfile(distance)
        val (targVels, _) = uni.speedTargetAngleTarget(speed, targetA)

        val nativeL = hardware.conversions.encoder.left.native(targVels.left)
        val nativeR = hardware.conversions.encoder.right.native(targVels.right)

        TwoSided(
            VelocityOutput(hardware.escConfig, velocityGains.left, nativeL),
            VelocityOutput(hardware.escConfig, velocityGains.right, nativeR)
        ).takeUnless {
            distance < tolerance
        }
    }
}

//suspend fun DrivetrainComponent.arcTo(
//        bearing: Angle, radius: Length,
//        angleTolerance: Angle, distanceTolerance: Length,
//
//        acceleration: Acceleration,
//        maxSpeed: Velocity,
//        deceleration: Acceleration = acceleration,
//        endingSpeed: Velocity = 0.FootPerSecond,
//        kickstart: Velocity = 3.Inch / Second
//) = startRoutine("arc") {
//    val position by hardware.position.readOnTick.withoutStamps
//    val velocity by hardware.velocity.readOnTick.withoutStamps
//    val gyro by hardware.gyroInput.readEagerly.withStamps
//
//    // s = r × θ
//    val theta = bearing - gyro.y.angle
//    val rL = radius + trackSize / 2
//    val rR = radius - trackSize / 2
//    val sL = rL * theta / Radian
//    val sR = rR * theta / Radian
//
//    val rSmall = rL minMag rR
//    val rBig = rL maxMag rR
//    val rSmallBigRatio = rSmall / rBig
//
//    val profile = trapezoidalMotionProfile(
//            distance = sL maxMag sR,
//            startingSpeed = kickstart maxMag velocity.avg,
//            acceleration = acceleration,
//            maxSpeed = maxSpeed,
//            deceleration = deceleration,
//            endingSpeed = endingSpeed
//    )
//
//    val startingPostion = position
//    val turnControl = pidControlLoop(::p, ::p, turningPositionGains) {
//        // θ = s ÷ r
//        (position.avg - startingPostion.avg) / radius * Radian
//    }
//
//    val slRange = sL `±` distanceTolerance
//    val srRange = sR `±` distanceTolerance
//    val bearingRange = bearing `±` angleTolerance
//
//    controller {
//        if (
//                position.left in slRange &&
//                position.right in srRange &&
//                gyro.y.angle in bearingRange
//        ) null
//        else {
//            val turn = turnControl(gyro.x, gyro.y.angle)
//
//            val dx = position - startingPostion
//            val bigTarget = profile(if (rBig == rL) dx.left else dx.right)
//
//            // s₂ = s₁ × (r₂ ÷ r₁)
//            // v₂ = v₁ × (r₂ ÷ r₁)
//            // a₂ = a₁ × (r₂ ÷ r₁)
//            val left = (if (rBig == rL) bigTarget else bigTarget * rSmallBigRatio) + turn
//            val right = (if (rBig == rR) bigTarget else bigTarget * rSmallBigRatio) - turn
//
//            hardware.offloadedSettings.run {
//                TwoSided(
//                        VelocityOutput(native(leftVelocityGains), native(left)),
//                        VelocityOutput(native(rightVelocityGains), native(right))
//                )
//            }
//        }
//    }
//}
//
//suspend fun DrivetrainComponent.driveStraight(
//        distance: Length, bearing: Angle,
//        distanceTolerance: Length, angleTolerance: Angle,
//
//        acceleration: Acceleration,
//        maxSpeed: Velocity,
//        deceleration: Acceleration = acceleration,
//        endingSpeed: Velocity = 0.FootPerSecond,
//        kickstart: Velocity = 3.Inch / Second
//) = startRoutine("straight") {
//    val position by hardware.position.readOnTick.withoutStamps
//    val velocity by hardware.velocity.readOnTick.withoutStamps
//    val gyro by hardware.gyroInput.readEagerly.withStamps
//
//    val profile = trapezoidalMotionProfile(
//            distance = distance,
//            startingSpeed = kickstart maxMag velocity.avg,
//            acceleration = acceleration,
//            maxSpeed = maxSpeed,
//            deceleration = deceleration,
//            endingSpeed = endingSpeed
//    )
//
//    val startingPostion = position
//    val turnControl = pidControlLoop(::p, ::p, turningPositionGains) { bearing }
//
//    val distanceRange = distance `±` distanceTolerance
//    val bearingRange = bearing `±` angleTolerance
//
//    controller {
//        if (
//                position.left in distanceRange &&
//                position.right in distanceRange &&
//                gyro.y.angle in bearingRange
//        ) null
//        else {
//            val turn = turnControl(gyro.x, gyro.y.angle)
//
//            val forward = profile((position - startingPostion).avg)
//            val left = forward + turn
//            val right = forward - turn
//
//            hardware.offloadedSettings.run {
//                TwoSided(
//                        VelocityOutput(native(leftVelocityGains), native(left)),
//                        VelocityOutput(native(rightVelocityGains), native(right))
//                )
//            }
//        }
//    }
//}
