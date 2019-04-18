package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.hardware.tickstoserial.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class UnicycleDrive(private val c: DrivetrainComponent, scope: BoundSensorScope) {
    val position by with(scope) { c.hardware.position.readOnTick.withStamps }
    val dadt = differentiator(::div, position.x, position.y.bearing)

    val errorGraph = c.graph("Error Angle", Degree)
    val speedGraph = c.graph("Target Speed", FootPerSecond)

    fun speedAngleTarget(speed: Velocity, angle: Angle): Pair<TwoSided<Velocity>, Angle> {
        val error = (angle `coterminal -` position.y.bearing)
        return speedTargetAngleError(speed, error) to error
    }

    fun speedTargetAngleError(speed: Velocity, error: Angle) = with(c) {
        val (t, p) = position

        val angularVelocity = dadt(t, p.bearing)

        val pA = bearingKp * error - bearingKd * angularVelocity

        val targetL = speed + pA
        val targetR = speed - pA

        TwoSided(targetL, targetR).also {
            speedGraph(t, it.avg)
            errorGraph(t, error)
        }
    }
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

        val (target, _) = uni.speedAngleTarget(forwardVelocity, absSteering + startingAngle)

        val nativeL = hardware.conversions.nativeConversion.native(
                target.left + steeringVelocity
        )
        val nativeR = hardware.conversions.nativeConversion.native(
                target.right - steeringVelocity
        )

        TwoSided(
                VelocityOutput(hardware.escConfig, velocityGains, nativeL),
                VelocityOutput(hardware.escConfig, velocityGains, nativeR)
        )
    }
}

suspend fun DrivetrainComponent.openLoop(power: DutyCycle) = startRoutine("open loop") {
    controller {
        TwoSided(
                PercentOutput(hardware.escConfig, power)
        )
    }
}

suspend fun DrivetrainComponent.turn(target: Angle, tolerance: Angle) = startRoutine("Turn") {
    val uni = UnicycleDrive(this@turn, this@startRoutine)

    controller {
        val (targVels, error) = uni.speedAngleTarget(0.FootPerSecond, target)

        val nativeL = hardware.conversions.nativeConversion.native(targVels.left)
        val nativeR = hardware.conversions.nativeConversion.native(targVels.right)

        TwoSided(
                VelocityOutput(hardware.escConfig, velocityGains, nativeL),
                VelocityOutput(hardware.escConfig, velocityGains, nativeR)
        ).takeUnless {
            error.abs < tolerance
        }
    }
}


suspend fun DrivetrainComponent.warmup() = startRoutine("Warmup") {

    fun r() = Math.random()
    val conv = DrivetrainConversions(hardware)

    controller {
        val startTime = currentTime
        while (currentTime - startTime < hardware.period * 60.Percent) {
            val (l, r) = TicksToSerialValue((r() * 0xFF).toInt())
            conv.accumulateOdometry(l, r)
        }
        val (x, y, _) = Position(conv.matrixTracking.x, conv.matrixTracking.y, conv.matrixTracking.bearing)


        val targetA = 1.Turn * r()
        val errorA = targetA `coterminal -` 1.Turn * r()
        val pA = bearingKp * errorA

        val targetL = maxSpeed * r() + pA + x / Second
        val targetR = maxSpeed * r() - pA + y / Second

        val nativeL = hardware.conversions.nativeConversion.native(targetL) * 0.001
        val nativeR = hardware.conversions.nativeConversion.native(targetR) * 0.001

        TwoSided(
                VelocityOutput(hardware.escConfig, velocityGains, nativeL),
                VelocityOutput(hardware.escConfig, velocityGains, nativeR)
        )
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
//    val turnControl = pidControlLoop(::div, ::times, turningPositionGains) {
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
//    val turnControl = pidControlLoop(::div, ::times, turningPositionGains) { bearing }
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
