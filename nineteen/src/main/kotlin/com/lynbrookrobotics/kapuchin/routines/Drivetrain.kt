package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.sqrt

suspend fun DrivetrainComponent.teleop(driver: DriverHardware) = startRoutine("teleop") {
    val accelerator by driver.accelerator.readWithEventLoop.withoutStamps
    val steering by driver.steering.readWithEventLoop.withoutStamps
    val absSteering by driver.absSteering.readWithEventLoop.withoutStamps

    val position by hardware.position.readOnTick.withStamps

    val targetGraph = graph("Target Angle", Degree)
    val errorGraph = graph("Error Angle", Degree)
    val speedGraph = graph("Teleop Speed", FootPerSecond)

    val speedL by hardware.leftSpeed.readOnTick.withoutStamps
    val speedR by hardware.rightSpeed.readOnTick.withoutStamps

    var startingAngle = -absSteering + position.y.bearing
    val dadt = differentiator(::div, position.x, position.y.bearing)

    controller { t ->
        if (
                speedL.isZero && speedR.isZero && accelerator.isZero && steering.isZero
        ) System.gc()


        val forwardVelocity = maxSpeed * accelerator
        val steeringVelocity = maxSpeed * steering

        if (box(steering) != 0.Percent) startingAngle = -absSteering + position.y.bearing

        val angularVelocity = dadt(position.x, position.y.bearing)
        val targetA = (absSteering + startingAngle).also { targetGraph(t, it) }
        val errorA = (targetA `coterminal -` position.y.bearing).also { errorGraph(t, it) }
        val pA = bearingKp * errorA - bearingKd * angularVelocity

        val targetL = forwardVelocity + steeringVelocity + pA
        val targetR = forwardVelocity - steeringVelocity - pA

        speedGraph(t, avg(targetL, targetR))

        val nativeL = hardware.conversions.nativeConversion.native(targetL)
        val nativeR = hardware.conversions.nativeConversion.native(targetR)

        TwoSided(
                VelocityOutput(velocityGains, nativeL),
                VelocityOutput(velocityGains, nativeR)
        )
    }
}

suspend fun DrivetrainComponent.pointWithLineScanner(speed: Velocity, lineScanner: LineScannerHardware) = startRoutine("point with line scanner") {
    val linePosition by lineScanner.linePosition.readOnTick.withoutStamps

    controller {
        val errorA = linePosition?.let {
            -atan(it / lineScannerLead)
        } ?: 0.Degree

        val pA = bearingKp * errorA

        val targetL = +pA + speed
        val targetR = -pA + speed

        val nativeL = hardware.conversions.nativeConversion.native(targetL)
        val nativeR = hardware.conversions.nativeConversion.native(targetR)

        TwoSided(
                VelocityOutput(velocityGains, nativeL),
                VelocityOutput(velocityGains, nativeR)
        )
    }
}

suspend fun DrivetrainComponent.waypoint(speed: Velocity, target: UomVector<Length>, tolerance: Length) = startRoutine("teleop") {
    val position by hardware.position.readOnTick.withStamps
    val dadt = differentiator(::div, position.x, position.y.bearing)

    val tolSq = tolerance * tolerance

    val targetGraph = graph("Target Angle", Degree)
    val errorGraph = graph("Error Angle", Degree)
    val waypointDistance = graph("Distance to Waypoint", Foot)

    controller { t ->
        val (pt, p) = position

        val angularVelocity = dadt(position.x, position.y.bearing)
        val targetA = atan2(target.x - p.x, target.y - p.y)
        val errorA = targetA `coterminal -` p.bearing
        val pA = bearingKp * errorA - bearingKd * angularVelocity

        targetGraph(pt, targetA)
        errorGraph(pt, errorA)

        val targetL = speed + pA
        val targetR = speed - pA


        val nativeL = hardware.conversions.nativeConversion.native(targetL)
        val nativeR = hardware.conversions.nativeConversion.native(targetR)

        TwoSided(
                VelocityOutput(velocityGains, nativeL),
                VelocityOutput(velocityGains, nativeR)
        ).takeIf {
            val dx = p.x - target.x
            val dy = p.y - target.y
            val distSq = dx * dx + dy * dy
            waypointDistance(t, Length(sqrt(distSq.siValue)))
            distSq > tolSq
        }
    }
}

suspend fun llAlign(
        drivetrain: DrivetrainComponent,
        limelight: LimelightHardware
) = startChoreo("ll align") {
    val distance by limelight.roughDistanceToTarget.readWithEventLoop().withoutStamps
    val angle by limelight.roughAngleToTarget.readWithEventLoop().withoutStamps
    val skew by limelight.roughSkewOfTarget.readWithEventLoop().withoutStamps

    val robotPosition by drivetrain.hardware.position.readWithEventLoop().withoutStamps
    val line = 25.Inch

    choreography {
        val sDist = distance
        val tx = angle
        val skw = skew

        if (sDist != null && tx != null && skw != null) {
            val target = UomVector(
                    sDist * sin(tx),
                    sDist * cos(tx)
            )
            val lineStart = UomVector(
                    line * sin(skw),
                    line * cos(skw)
            )
            val current = robotPosition.vector // absolute

            /*- UomVector(
                    line * cos(snapshot.bearing),
                    line * sin(snapshot.bearing)
            )*/


            drivetrain.waypoint(3.FootPerSecond, current + target - lineStart, 4.Inch)
            drivetrain.waypoint(3.FootPerSecond, current + target, 6.Inch)
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