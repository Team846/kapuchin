package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.hardware.tickstoserial.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.LimelightHardware
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
    val accelerator by driver.accelerator.readWithEventLoop.withoutStamps
    val steering by driver.steering.readWithEventLoop.withoutStamps
    val absSteering by driver.absSteering.readWithEventLoop.withoutStamps

    val position by hardware.position.readOnTick.withStamps

    val uni = UnicycleDrive(this@teleop, this@startRoutine)

    val speedL by hardware.leftSpeed.readOnTick.withoutStamps
    val speedR by hardware.rightSpeed.readOnTick.withoutStamps

    var startingAngle = -absSteering + position.y.bearing

    controller { t ->
        if (
                speedL.isZero && speedR.isZero && accelerator.isZero && steering.isZero
        ) System.gc()


        val forwardVelocity = maxSpeed * accelerator
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
                VelocityOutput(velocityGains, nativeL),
                VelocityOutput(velocityGains, nativeR)
        )
    }
}

suspend fun DrivetrainComponent.pointWithLineScanner(speed: Velocity, lineScanner: LineScannerHardware) = startRoutine("Point with line scanner") {
    val linePosition by lineScanner.linePosition.readOnTick.withoutStamps
    val uni = UnicycleDrive(this@pointWithLineScanner, this@startRoutine)

    controller {
        val errorA = linePosition?.let {
            -atan(it / lineScannerLead)
        } ?: 0.Degree

        val (targetL, targetR) = uni.speedTargetAngleError(speed, errorA)

        val nativeL = hardware.conversions.nativeConversion.native(targetL)
        val nativeR = hardware.conversions.nativeConversion.native(targetR)

        TwoSided(
                VelocityOutput(velocityGains, nativeL),
                VelocityOutput(velocityGains, nativeR)
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
                VelocityOutput(velocityGains, nativeL),
                VelocityOutput(velocityGains, nativeR)
        ).takeIf {
            error !in 0.Degree `±` tolerance
        }
    }
}

/*suspend fun DrivetrainComponent.limelightTurn(target: Angle, tolerance: Angle, limelight: LimelightHardware) = startRoutine("Limelight Turn") {
    val uni = UnicycleDrive(this@limelightTurn, this@startRoutine)

    controller {
        val (targVels, error) = uni.speedAngleTarget(0.FootPerSecond, target)

        val nativeL = hardware.conversions.nativeConversion.native(targVels.left)
        val nativeR = hardware.conversions.nativeConversion.native(targVels.right)

        TwoSided(
                VelocityOutput(velocityGains, nativeL),
                VelocityOutput(velocityGains, nativeR)
        ).takeIf {
            error !in 0.Degree `±` tolerance
        }
    }
}
*/
suspend fun DrivetrainComponent.limelightTracking(speed: Velocity, limelight: LimelightHardware) = startRoutine("Limelight tracking") {
    val targetAngle by limelight.targetAngle.readOnTick.withoutStamps
    val robotPosition by hardware.position.readOnTick.withoutStamps
    val uni = UnicycleDrive(this@limelightTracking, this@startRoutine)

    val target = targetAngle?.let { it + robotPosition.bearing }
    controller {
        if (target != null) {
            val (targs, _) = uni.speedAngleTarget(speed, target)

            val nativeL = hardware.conversions.nativeConversion.native(targs.left)
            val nativeR = hardware.conversions.nativeConversion.native(targs.right)

            TwoSided(
                    VelocityOutput(velocityGains, nativeL),
                    VelocityOutput(velocityGains, nativeR)
            )
        } else null
    }
}

//suspend fun DrivetrainComponent.limelightTurnToIsosceles(limelight: LimelightHardware, speedMultiplier: Double) = startRoutine("Turn to Median") {
//    val distToNorm by limelight.distanceToNormal.readOnTick.withoutStamps
//    val targetLocation by limelight.targetPosition.readOnTick.withoutStamps
//    val distanceToTarget = sqrt(Dimensionless((targetLocation!!.x.siValue * targetLocation!!.x.siValue) + (targetLocation!!.y.siValue * targetLocation!!.y.siValue)))
//
//    val targetStatus by limelight.targetStatus.readOnTick.withoutStamps
//
//    val outerVelocity = -maxSpeed * speedMultiplier
//    val innerVelocity = maxSpeed * speedMultiplier
//    val angularVelocity = maxOmega*speedMultiplier
//
//    val startingTXValue by limelight.angleToTarget.readOnTick.withoutStamps
//    val startingSideAcrossTX = sqrt((distanceToTarget * distanceToTarget) + (Dimensionless(distToNorm!!.siValue) * Dimensionless(distToNorm!!.siValue)) - (2 * distToNorm!!.siValue * distanceToTarget.siValue * cos(startingTXValue!!)))
//    val startingIsosAngle = acos(((distanceToTarget * distanceToTarget) + (startingSideAcrossTX * startingSideAcrossTX) - Dimensionless(distToNorm!!.siValue * distToNorm!!.siValue)) / (Dimensionless(2.0) * distanceToTarget * distToNorm!!.siValue).siValue)
//    val startingTurnAngle = startingIsosAngle - startingTXValue!!

//    controller {
//        val txValue by limelight.angleToTarget.readOnTick.withoutStamps
//        val sideAcrossTX = sqrt((distanceToTarget * distanceToTarget) + (Dimensionless(distToNorm!!.siValue) * Dimensionless(distToNorm!!.siValue)) - (2 * distToNorm!!.siValue * distanceToTarget!!.siValue * cos(txValue!!)))
//        val isosAngle = acos(((distanceToTarget * distanceToTarget) + (sideAcrossTX * sideAcrossTX) - Dimensionless(distToNorm!!.siValue * distToNorm!!.siValue)) / (Dimensionless(2.0) * distanceToTarget * distToNorm!!.siValue).siValue)
//        val currentTurnAngle = isosAngle - txValue!!
//        val nativeL = hardware.conversions.nativeConversion.native(outerVelocity)
//        val nativeR = hardware.conversions.nativeConversion.native(innerVelocity)
//
//        if (targetStatus != null && targetStatus == true && currentTurnAngle.siValue != 0.0 && corner1!!.abs.Foot > lastCorner!!.abs.Foot){
//                TwoSided(
//                        VelocityOutput(velocityGains, nativeL * (currentTurnAngle / startingTurnAngle).siValue),
//                        VelocityOutput(velocityGains, nativeR * (currentTurnAngle / startingTurnAngle).siValue)
//                )
//        } else null
//    }
//
//}

suspend fun DrivetrainComponent.limelightCurveDrive(limelight: LimelightHardware,
                                                    trackLength: Length,
                                                    speedMultiplier: Double
) = startRoutine("Curve Drive to Target") {
    val txValue by limelight.angleToTarget.readOnTick.withoutStamps
    val tVert = limelight.l("tvert")
    val distanceToTarget = limelight.distanceToTarget(tVert)
    val targetExists by limelight.targetStatus.readOnTick.withoutStamps

    val angleToTarget by limelight.angleToTarget.readOnTick.withoutStamps
    val converter = kotlin.math.PI / 180.0

    val targetAngle = angleToTarget!!.Radian * converter.Radian

    val innerLength = ((distanceToTarget * (targetAngle / 1.Radian) / sin(targetAngle / converter)) - (trackLength ) * (targetAngle / 1.Radian))
    val outerLength = ((distanceToTarget * (targetAngle / 1.Radian) / sin(targetAngle / converter)) + (trackLength) * (targetAngle / 1.Radian))

    val outerVelocity = maxSpeed * speedMultiplier
    val innerVelocity = (outerVelocity * innerLength / outerLength)
    controller {
        if (targetExists != null && targetExists == true && txValue!!.Degree < 0.0) {

            val nativeL = hardware.conversions.nativeConversion.native(innerVelocity)
            val nativeR = hardware.conversions.nativeConversion.native(outerVelocity)

            TwoSided(
                    VelocityOutput(velocityGains, nativeL),
                    VelocityOutput(velocityGains, nativeR)
            )
        } else if (targetExists != null && targetExists == true && txValue!!.Degree >= 0.0) {

            val nativeL = hardware.conversions.nativeConversion.native(outerVelocity)
            val nativeR = hardware.conversions.nativeConversion.native(innerVelocity)

            TwoSided(
                    VelocityOutput(velocityGains, nativeL),
                    VelocityOutput(velocityGains, nativeR)
            )
        } else null

    }
}


suspend fun DrivetrainComponent.warmup() = startRoutine("Warmup") {

    fun r() = Math.random()
    val conv = DrivetrainConversions(hardware)

    controller {
        val startTime = currentTime
        while (currentTime - startTime < hardware.period * 90.Percent) {
            val (l, r) = TicksToSerialValue((r() * 0xFF).toInt())
            conv.accumulateOdometry(l, r)
        }
        val (x, y, _) = Position(conv.matrixTracking.x, conv.matrixTracking.y, conv.matrixTracking.bearing)


        val targetA = 1.Turn * r()
        val errorA = targetA `coterminal -` 1.Turn * r()
        val pA = bearingKp * errorA

        val targetL = maxSpeed * r() + pA + x / Second
        val targetR = maxSpeed * r() - pA + y / Second

        val nativeL = hardware.conversions.nativeConversion.native(targetL) * 0.01
        val nativeR = hardware.conversions.nativeConversion.native(targetR) * 0.01

        TwoSided(
                VelocityOutput(velocityGains, nativeL),
                VelocityOutput(velocityGains, nativeR)
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
