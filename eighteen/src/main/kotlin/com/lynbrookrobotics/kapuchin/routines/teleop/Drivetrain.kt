package com.lynbrookrobotics.kapuchin.routines.teleop

import com.lynbrookrobotics.kapuchin.control.electrical.RampRateLimiter
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidControlLoop
import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.control.math.avg
import com.lynbrookrobotics.kapuchin.control.math.kinematics.TrapezoidalMotionProfile
import com.lynbrookrobotics.kapuchin.control.math.minus
import com.lynbrookrobotics.kapuchin.control.maxMag
import com.lynbrookrobotics.kapuchin.control.minMag
import com.lynbrookrobotics.kapuchin.control.withToleranceOf
import com.lynbrookrobotics.kapuchin.hardware.offloaded.PositionOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VelocityOutput
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.LiftComponent
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import info.kunalsheth.units.generated.*

suspend fun DrivetrainComponent.teleop(driver: DriverHardware, lift: LiftComponent) {
    val accelerator by driver.accelerator.readOnTick.withoutStamps
    val steering by driver.steering.readOnTick.withoutStamps

    val liftHeight by lift.hardware.position.readOnTick.withoutStamps
    val liftActivationThreshold = lift.collectHeight + lift.positionTolerance

    val slewFunction: (Time) -> Acceleration = {
        if (liftHeight > liftActivationThreshold) maxAccelerationWithLiftUp / (liftHeight / lift.hardware.maxHeight)
        else 1000.FootPerSecondSquared
    }

    val leftSlew = RampRateLimiter(limit = slewFunction)
    val rightSlew = RampRateLimiter(limit = slewFunction)

    runRoutine("Teleop") {
        val left = leftSlew(it, topSpeed * (accelerator + steering))
        val right = rightSlew(it, topSpeed * (accelerator - steering))

        hardware.offloadedSettings.run {
            TwoSided(
                    VelocityOutput(native(leftVelocityGains), native(left)),
                    VelocityOutput(native(rightVelocityGains), native(right))
            )
        }
    }
}

suspend fun DrivetrainComponent.arcTo(
        bearing: Angle, radius: Length,
        angleTolerance: Angle, distanceTolerance: Length,

        acceleration: Acceleration,
        topSpeed: Velocity,
        deceleration: Acceleration = acceleration,
        endingSpeed: Velocity = 0.FootPerSecond,
        kickstart: Velocity = 3.Inch / Second
) {
    val position by hardware.position.readOnTick.withoutStamps
    val velocity by hardware.velocity.readOnTick.withoutStamps
    val gyro by hardware.gyroInput.readEagerly.withStamps

    // s = r × θ
    val theta = bearing - gyro.value.angle
    val rL = radius + trackSize / 2
    val rR = radius - trackSize / 2
    val sL = rL * theta
    val sR = rR * theta

    val rSmall = rL minMag rR
    val rBig = rL maxMag rR
    val rSmallBigRatio = rSmall / rBig

    val profile = TrapezoidalMotionProfile(
            distance = sL maxMag sR,
            startingSpeed = kickstart maxMag velocity.avg,
            acceleration = acceleration,
            topSpeed = topSpeed,
            deceleration = deceleration,
            endingSpeed = endingSpeed
    )

    val startingPostion = position
    val turnControl = PidControlLoop(turningPositionGains) {
        // θ = s ÷ r
        (position.avg - startingPostion.avg) / radius
    }

    val slRange = sL withToleranceOf distanceTolerance
    val srRange = sR withToleranceOf distanceTolerance
    val bearingRange = bearing withToleranceOf angleTolerance
    runRoutine("Arc") {
        if (
                position.left in slRange &&
                position.right in srRange &&
                gyro.value.angle in bearingRange
        ) null
        else {
            val turn = turnControl(gyro.stamp, gyro.value.angle)

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

suspend fun DrivetrainComponent.driveStraightTrapezoidal(
        distance: Length, bearing: Angle,
        distanceTolerance: Length, angleTolerance: Angle,

        acceleration: Acceleration,
        topSpeed: Velocity,
        deceleration: Acceleration = acceleration,
        endingSpeed: Velocity = 0.FootPerSecond,
        kickstart: Velocity = 6.Inch / Second
) {
    val position by hardware.position.readOnTick.withoutStamps
    val velocity by hardware.velocity.readOnTick.withoutStamps
    val gyro by hardware.gyroInput.readEagerly.withStamps

    val profile = TrapezoidalMotionProfile(
            distance = distance,
            startingSpeed = kickstart maxMag velocity.avg,
            acceleration = acceleration,
            topSpeed = topSpeed,
            deceleration = deceleration,
            endingSpeed = endingSpeed
    )

    val startingPostion = position
    val turnControl = PidControlLoop(turningPositionGains) { bearing }

    val distanceRange = distance withToleranceOf distanceTolerance
    val bearingRange = bearing withToleranceOf angleTolerance

    runRoutine("Straight") {
        if (
                position.avg in distanceRange &&
                gyro.value.angle in bearingRange
        ) null
        else {
            val turn = turnControl(gyro.stamp, gyro.value.angle)

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

suspend fun DrivetrainComponent.driveStraightPid(
        distance: Length, distanceTolerance: Length
) {
    val position by hardware.position.readOnTick.withoutStamps
    val distanceRange = distance withToleranceOf distanceTolerance

    val left = position.left + distance
    val right = position.right + distance

    runRoutine("Straight") {
        if (position.avg in distanceRange) null
        else hardware.offloadedSettings.run {
            TwoSided(
                    PositionOutput(native(positionGains), native(left)),
                    PositionOutput(native(positionGains), native(right))
            )
        }
    }
}