package com.lynbrookrobotics.kapuchin.routines.teleop

import com.lynbrookrobotics.kapuchin.control.electrical.RampRateLimiter
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidControlLoop
import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.control.math.avg
import com.lynbrookrobotics.kapuchin.control.math.integration.InfiniteIntegrator
import com.lynbrookrobotics.kapuchin.control.math.kinematics.TrapezoidalMotionProfile
import com.lynbrookrobotics.kapuchin.control.math.minus
import com.lynbrookrobotics.kapuchin.control.maxMag
import com.lynbrookrobotics.kapuchin.control.minMag
import com.lynbrookrobotics.kapuchin.control.withToleranceOf
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VelocityOutput
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.LiftComponent
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import info.kunalsheth.units.generated.*
import kotlin.math.sign

suspend fun DrivetrainComponent.teleop(driver: DriverHardware, lift: LiftComponent) {
    val accelerator by driver.accelerator.readOnTick.withoutStamps
    val steering by driver.steering.readOnTick.withoutStamps
    val gyro by hardware.gyroInput.readEagerly.withoutStamps

    val liftHeight by lift.hardware.position.readOnTick.withoutStamps
    val liftActivationThreshold = lift.collectHeight withToleranceOf lift.positionTolerance

    val slewFunction: (Time) -> Acceleration = {
        if (liftHeight !in liftActivationThreshold) maxAccelerationWithLiftUp / (liftHeight / lift.hardware.maxHeight)
        else 1.giga { FootPerSecondSquared }
    }

    val leftSlew = RampRateLimiter(limit = slewFunction)
    val rightSlew = RampRateLimiter(limit = slewFunction)

    val turnTargetIntegrator = InfiniteIntegrator(gyro.angle)
    val turnControl = PidControlLoop(turningPositionGains) {
        if (steering == 0.0) turnTargetIntegrator(it, 0.DegreePerSecond)
        else turnTargetIntegrator(it, maxTurningSpeed * steering / (accelerator + steering))
    }

    fun sqrWithSign(x: Double) = x * x * x.sign

    runRoutine("Teleop") {
        val forwardVelocity = topSpeed * sqrWithSign(accelerator)
        val steeringVelocity = topSpeed * steering + turnControl(it, gyro.angle)

        val left = leftSlew(it, forwardVelocity + steeringVelocity)
        val right = rightSlew(it, forwardVelocity - steeringVelocity)

        hardware.offloadedSettings.run {
            TwoSided(
                    VelocityOutput(native(leftVelocityGains), native(left)),
                    VelocityOutput(native(rightVelocityGains), native(right))
            )
        }
    }
}

suspend fun DrivetrainComponent.arc(
        distance: Length, distanceTolerance: Length,
        radius: Length, angleTolerance: Angle,
        acceleration: Acceleration,
        topSpeed: Velocity,
        deceleration: Acceleration = acceleration,
        endingSpeed: Velocity = 0.FootPerSecond,
        kickstart: Velocity = 3.Inch / 1.Second
) {
    // s = rθ
    val theta = distance / radius
    val rL = (radius + trackSize / 2)
    val rR = (radius - trackSize / 2)

    // s₁ * (r₂ ÷ r₁) = s₂
    // v₁ * (r₂ ÷ r₁) = v₂
    // a₁ * (r₂ ÷ r₁) = a₂
    val rlRatio = rL / radius
    val rrRatio = rR / radius
    val sL = distance * rlRatio
    val sR = distance * rrRatio

    val minMagR = rL minMag rR
    val maxMagR = rL maxMag rR
    val shortLongRatio = minMagR / maxMagR

    val longerProfile = TrapezoidalMotionProfile(
            distance = sL maxMag sR,
            startingSpeed = kickstart,
            acceleration = acceleration,
            topSpeed = topSpeed,
            deceleration = deceleration,
            endingSpeed = endingSpeed
    )
    val shorterProfile = TrapezoidalMotionProfile(
            distance = sL minMag sR,
            startingSpeed = kickstart * shortLongRatio,
            acceleration = acceleration * shortLongRatio,
            topSpeed = topSpeed * shortLongRatio,
            deceleration = deceleration * shortLongRatio,
            endingSpeed = endingSpeed * shortLongRatio
    )
    val leftProfile = if (maxMagR == rL) longerProfile else shorterProfile
    val rightProfile = if (maxMagR == rR) longerProfile else shorterProfile

    val leftGains = hardware.offloadedSettings.native(leftVelocityGains)
    val rightGains = hardware.offloadedSettings.native(rightVelocityGains)

    val position by hardware.position.readOnTick.withoutStamps
    val startingPosition = position
    fun deltaPosition() = position - startingPosition

    val gyro by hardware.gyroInput.readEagerly.withStamps
    val startingDirection = gyro.value.angle
    fun deltaDirection() = gyro.value.angle - startingDirection

    // θ = s ÷ r
    val turningControl = PidControlLoop(turningPositionGains) {
        (startingPosition - position).avg / radius
    }

    runRoutine("Arc") {
        if (
                deltaPosition().avg in distance withToleranceOf distanceTolerance &&
                deltaDirection() in theta withToleranceOf angleTolerance
        ) null
        else {
            val turn = turningControl(gyro.stamp, deltaDirection())

            val deltaPosition = deltaPosition()
            val leftTarget = leftProfile(deltaPosition.left) + turn
            val rightTarget = rightProfile(deltaPosition.right) - turn

            TwoSided(
                    VelocityOutput(leftGains, hardware.offloadedSettings.native(leftTarget)),
                    VelocityOutput(rightGains, hardware.offloadedSettings.native(rightTarget))
            )
        }
    }
}