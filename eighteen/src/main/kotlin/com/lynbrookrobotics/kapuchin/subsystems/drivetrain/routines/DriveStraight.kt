package com.lynbrookrobotics.kapuchin.subsystems.drivetrain.routines

import com.lynbrookrobotics.kapuchin.control.loops.pid.PidControlLoop
import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.control.math.kinematics.TrapezoidalMotionProfile
import com.lynbrookrobotics.kapuchin.control.maxMag
import com.lynbrookrobotics.kapuchin.control.minMag
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VelocityOutput
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import info.kunalsheth.units.generated.*

fun DrivetrainComponent.drive(
        distance: Length, radius: Length,
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
            distance = maxMagR,
            startingSpeed = kickstart,
            acceleration = acceleration,
            topSpeed = topSpeed,
            deceleration = deceleration,
            endingSpeed = endingSpeed
    )
    val shorterProfile = TrapezoidalMotionProfile(
            distance = minMagR,
            startingSpeed = kickstart * shortLongRatio,
            acceleration = acceleration * shortLongRatio,
            topSpeed = topSpeed * shortLongRatio,
            deceleration = deceleration * shortLongRatio,
            endingSpeed = endingSpeed * shortLongRatio
    )
    val leftProfile = if (maxMagR == rL) longerProfile else shorterProfile
    val rightProfile = if (maxMagR == rR) longerProfile else shorterProfile

    val gains = offloadedSettings.native(velocityGains)

    val startingPosition = forwardPosition
    fun deltaPosition() = forwardPosition.value - startingPosition.value

    val direction by hardware.gyro
    val startingDirection = direction.value.angle
    fun deltaDirection() = direction.value.angle - startingDirection

    // θ = s ÷ r
    val turningControl = PidControlLoop(turningPositionGains) {
        radius.let { deltaPosition() / it }
    }

    return DrivetrainSubroutine(
            "Drive Straight", this,
            {
                val turn = turningControl(direction.stamp, deltaDirection())

                val leftTarget = leftProfile(deltaPosition()) + turn
                val rightTarget = rightProfile(deltaPosition()) - turn

                TwoSided(
                        VelocityOutput(gains, offloadedSettings.native(leftTarget)),
                        VelocityOutput(gains, offloadedSettings.native(rightTarget))
                )
            },
            {

            }
    )
}