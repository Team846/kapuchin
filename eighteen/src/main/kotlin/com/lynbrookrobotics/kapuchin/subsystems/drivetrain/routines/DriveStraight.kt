package com.lynbrookrobotics.kapuchin.subsystems.drivetrain.routines

import com.lynbrookrobotics.kapuchin.control.loops.pid.PidControlLoop
import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.control.math.avg
import com.lynbrookrobotics.kapuchin.control.math.kinematics.TrapezoidalMotionProfile
import com.lynbrookrobotics.kapuchin.control.math.minus
import com.lynbrookrobotics.kapuchin.control.maxMag
import com.lynbrookrobotics.kapuchin.control.minMag
import com.lynbrookrobotics.kapuchin.control.withToleranceOf
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VelocityOutput
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import info.kunalsheth.units.generated.*

fun DrivetrainComponent.drive(
        distance: Length, distanceTolerance: Length,
        radius: Length, angleTolerance: Angle,
        acceleration: Acceleration,
        topSpeed: Velocity,
        deceleration: Acceleration = acceleration,
        endingSpeed: Velocity = 0.FootPerSecond,
        kickstart: Velocity = 3.Inch / 1.Second
): DrivetrainSubroutine {
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

    val gains = offloadedSettings.native(velocityGains)

    val startingPosition = position
    fun deltaPosition() = position - startingPosition

    val gyro by hardware.gyro.withTimeStamps
    val startingDirection = gyro.value.angle
    fun deltaDirection() = gyro.value.angle - startingDirection

    // θ = s ÷ r
    val turningControl = PidControlLoop(turningPositionGains) {
        (startingPosition - position).avg / radius
    }

    return DrivetrainSubroutine(
            "Drive Straight", this,
            {
                val turn = turningControl(gyro.stamp, deltaDirection())

                val deltaPosition = deltaPosition()
                val leftTarget = leftProfile(deltaPosition.left) + turn
                val rightTarget = rightProfile(deltaPosition.right) - turn

                TwoSided(
                        VelocityOutput(gains, offloadedSettings.native(leftTarget)),
                        VelocityOutput(gains, offloadedSettings.native(rightTarget))
                )
            },
            {
                deltaPosition().avg in distance withToleranceOf distanceTolerance &&
                        deltaDirection() in theta withToleranceOf angleTolerance
            }
    )
}