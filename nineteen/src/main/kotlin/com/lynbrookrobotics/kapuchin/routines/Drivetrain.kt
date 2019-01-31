package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.conversion.deadband.verticalDeadband
import com.lynbrookrobotics.kapuchin.control.data.TwoSided
import com.lynbrookrobotics.kapuchin.control.electrical.motorCurrentLimiter
import com.lynbrookrobotics.kapuchin.control.electrical.voltageToDutyCycle
import com.lynbrookrobotics.kapuchin.control.math.`coterminal -`
import com.lynbrookrobotics.kapuchin.control.math.differentiator
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.ElectricalSystemHardware
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.abs

suspend fun DrivetrainComponent.teleop(driver: DriverHardware, electrical: ElectricalSystemHardware) = startRoutine("teleop") {
    val accelerator by driver.accelerator.readWithEventLoop.withoutStamps
    val steering by driver.steering.readWithEventLoop.withoutStamps
    val absSteering by driver.absSteering.readWithEventLoop.withoutStamps

    val position by hardware.position.readOnTick.withoutStamps
    var startingAngle = -absSteering + position.xy.bearing

    val speedL by hardware.leftSpeed.readOnTick.withoutStamps
    val speedR by hardware.rightSpeed.readOnTick.withoutStamps

    val startupFrictionCompensation = verticalDeadband(startupVoltage, operatingVoltage)
    val currentLimiting = motorCurrentLimiter(operatingVoltage, maxSpeed, motorStallCurrent, motorCurrentLimit)
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        val forwardVelocity = maxSpeed * accelerator
        val steeringVelocity = maxSpeed * steering

        val currentAngle = position.xy.bearing
        if (box(steering) != 0.Percent) startingAngle = -absSteering + currentAngle

        val errorA = absSteering + startingAngle `coterminal -` currentAngle
        val pA = bearingKp * errorA

        val targetL = forwardVelocity + steeringVelocity + pA
        val targetR = forwardVelocity - steeringVelocity - pA

        val errorL = targetL - speedL
        val errorR = targetR - speedR

        val pL = velocityKp * errorL
        val pR = velocityKp * errorR

        val ffL = targetL / maxLeftSpeed * operatingVoltage
        val ffR = targetR / maxRightSpeed * operatingVoltage

        val dcL = voltageToDutyCycle(
                currentLimiting(speedL,
                        startupFrictionCompensation(pL + ffL)
                ), vBat
        )

        val dcR = voltageToDutyCycle(
                currentLimiting(speedR,
                        startupFrictionCompensation(pR + ffR)
                ), vBat
        )

        TwoSided(dcL, dcR)
    }
}

suspend fun DrivetrainComponent.noEncoderTeleop(driver: DriverHardware, electrical: ElectricalSystemHardware) = startRoutine("no encoder teleop") {
    val accelerator by driver.accelerator.readWithEventLoop.withoutStamps
    val steering by driver.steering.readWithEventLoop.withoutStamps
    val absSteering by driver.absSteering.readWithEventLoop.withStamps

    val gyro by hardware.gyroInput.readEagerly.withoutStamps
    var targetA = gyro.angle

    val startupFrictionCompensation = verticalDeadband(startupVoltage, operatingVoltage)
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    val absSteeringRate = differentiator(::div, currentTime, absSteering.y)
    var absSteeringStart = absSteering.y

    controller {
        val forwardVelocity = maxSpeed * accelerator
        val steeringVelocity = maxSpeed * steering

        val absSteeringMode = abs(absSteeringRate(absSteering.x, absSteering.y)) > 30.DegreePerSecond
        absSteeringStart = when {
            absSteeringMode -> absSteeringStart
            else -> gyro.angle + absSteering.y
        }

        targetA = when {
            absSteeringMode -> absSteeringStart
            steering == 0.Percent -> targetA
            else -> gyro.angle
        }

        val errorA = targetA `coterminal -` gyro.angle
        val pA = bearingKp * errorA

        val targetL = forwardVelocity + steeringVelocity + pA
        val targetR = forwardVelocity - steeringVelocity - pA

        val ffL = targetL / maxLeftSpeed * operatingVoltage
        val ffR = targetR / maxRightSpeed * operatingVoltage

        val dcL = voltageToDutyCycle(
                startupFrictionCompensation(ffL), vBat
        )

        val dcR = voltageToDutyCycle(
                startupFrictionCompensation(ffR), vBat
        )

        TwoSided(dcL, dcR)
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