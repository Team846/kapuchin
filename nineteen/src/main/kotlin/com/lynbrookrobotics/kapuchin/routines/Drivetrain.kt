package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.hardware.tickstoserial.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.control.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class UnicycleDrive(private val c: Drivetrain, scope: BoundSensorScope) {
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

fun target(current: Waypt, target: Waypt) = atan2(target.x - current.x, target.y - current.y)

fun Drivetrain.teleop(driver: Driver) = newRoutine("Teleop") {
    val accelerator by driver.accelerator.readOnTick.withoutStamps
    val steering by driver.steering.readOnTick.withoutStamps
    val absSteering by driver.absSteering.readOnTick.withoutStamps

    val position by hardware.position.readOnTick.withStamps

    val uni = UnicycleDrive(this@teleop, this@newRoutine)

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

fun Drivetrain.openLoop(power: DutyCycle) = newRoutine("Open loop") {
    controller { TwoSided(PercentOutput(hardware.escConfig, power)) }
}

fun Drivetrain.turn(target: Angle, tolerance: Angle) = newRoutine("Turn") {
    val uni = UnicycleDrive(this@turn, this@newRoutine)

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


fun Drivetrain.warmup() = newRoutine("Warmup") {

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

fun Drivetrain.followTrajectory(
        tolerance: Length, endTolerance: Length,
        deceleration: Acceleration, waypts: List<TimeStamped<Waypt>>,
        origin: Position = hardware.position.optimizedRead(currentTime, 0.Second).y
) = newRoutine("Read journal") {
    val position by hardware.position.readOnTick.withStamps
    val uni = UnicycleDrive(this@followTrajectory, this@newRoutine)

    val waypointDistance = graph("Distance to Waypoint", Foot)

    val startingLoc = origin.vector
    val mtrx = RotationMatrix(origin.bearing)

    var remainingDistance = waypts
            .zipWithNext { a, b -> distance(a.y, b.y) }
            .reduce { a, b -> a + b }

    val targIter = waypts
            .map { (t, pt) -> (mtrx rz pt) + startingLoc stampWith t }
            .iterator()

    var target = targIter.next()
    var speed = 1.FootPerSecond
    var isDone = false

    controller { t ->
        val (_, p) = position
        val location = p.vector

        val distanceToNext = distance(location, target.y).also { waypointDistance(t, it) }
        if (!targIter.hasNext() && distanceToNext < endTolerance) {
            isDone = true
            speed = 0.FootPerSecond
        } else if (targIter.hasNext() && distanceToNext < tolerance) {
            val newTarget = targIter.next()

            val dist = distance(newTarget.y, target.y)
            speed = avg(
                    speed, dist / (newTarget.x - target.x)
            )
            remainingDistance -= dist
            target = newTarget
        }

        val targetA = target(location, target.y)
        val (targVels, _) = uni.speedAngleTarget(
                speed min v(deceleration, 0.FootPerSecond, remainingDistance + distanceToNext),
                targetA
        )

        val nativeL = hardware.conversions.nativeConversion.native(targVels.left)
        val nativeR = hardware.conversions.nativeConversion.native(targVels.right)

        TwoSided(
                VelocityOutput(hardware.escConfig, velocityGains, nativeL),
                VelocityOutput(hardware.escConfig, velocityGains, nativeR)
        ).takeUnless { isDone }
    }
}

fun Drivetrain.waypoint(
        motionProfile: (Length) -> Velocity,
        target: UomVector<Length>,
        tolerance: Length
) = newRoutine("Waypoint") {
    val position by hardware.position.readOnTick.withStamps
    val uni = UnicycleDrive(this@waypoint, this@newRoutine)

    val waypointDistance = graph("Distance to Waypoint", Foot)

    controller { t ->
        val (_, p) = position
        val location = p.vector

        val distance = distance(location, target).also { waypointDistance(t, it) }

        val targetA = target(location, target)
        val speed = motionProfile(distance)
        val (targVels, _) = uni.speedAngleTarget(speed, targetA)

        val nativeL = hardware.conversions.nativeConversion.native(targVels.left)
        val nativeR = hardware.conversions.nativeConversion.native(targVels.right)

        TwoSided(
                VelocityOutput(hardware.escConfig, velocityGains, nativeL),
                VelocityOutput(hardware.escConfig, velocityGains, nativeR)
        ).takeUnless {
            distance < tolerance
        }
    }
}

fun Drivetrain.visionSnapshotTracking(
        speed: Velocity,
        limelight: Limelight
) = newRoutine("Vision snapshot tracking") {
    val targetAngle by limelight.targetAngle.readOnTick.withoutStamps
    val robotPosition by hardware.position.readOnTick.withoutStamps
    val uni = UnicycleDrive(this@visionSnapshotTracking, this@newRoutine)

    val target = targetAngle?.let { it + robotPosition.bearing }

    controller {
        if (target != null) {
            val (targs, _) = uni.speedAngleTarget(speed, target)

            val nativeL = hardware.conversions.nativeConversion.native(targs.left)
            val nativeR = hardware.conversions.nativeConversion.native(targs.right)

            TwoSided(
                    VelocityOutput(hardware.escConfig, velocityGains, nativeL),
                    VelocityOutput(hardware.escConfig, velocityGains, nativeR)
            )
        } else null
    }
}

fun Drivetrain.visionActiveTracking(
        motionProfile: (Length) -> Velocity,
        limelight: Limelight,
        tolerance: Length
) = newRoutine("Vision snapshot tracking") {
    val targetAngle by limelight.targetAngle.readOnTick.withoutStamps
    val targetPosition by limelight.targetPosition.readOnTick.withoutStamps
    val robotPosition by hardware.position.readOnTick.withoutStamps
    val uni = UnicycleDrive(this@visionActiveTracking, this@newRoutine)

    controller {
        targetAngle?.let { targetSnapshot ->
            val distance = targetPosition!!.vector.abs

            val (targs, _) = uni.speedAngleTarget(motionProfile(distance), targetSnapshot + robotPosition.bearing)

            val nativeL = hardware.conversions.nativeConversion.native(targs.left)
            val nativeR = hardware.conversions.nativeConversion.native(targs.right)

            TwoSided(
                    VelocityOutput(hardware.escConfig, velocityGains, nativeL),
                    VelocityOutput(hardware.escConfig, velocityGains, nativeR)
            ).takeIf {
                distance > tolerance
            }
        }
    }
}

fun Drivetrain.lineActiveTracking(
        speed: Velocity,
        targetRange: ClosedRange<Length>,
        lineScanner: LineScanner
) = newRoutine("Point with line scanner") {
    val linePosition by lineScanner.linePosition.readOnTick.withoutStamps
    val uni = UnicycleDrive(this@lineActiveTracking, this@newRoutine)

    var targetLinePosition: Length? = null

    controller {
        if (targetLinePosition == null) {
            linePosition?.let { lineSnapshot ->
                targetLinePosition = lineSnapshot cap targetRange
                log(Debug) { "Holding line at ${(targetLinePosition ?: 0.Inch).Inch withDecimals 2} inches" }
            }
            TwoSided(VelocityOutput(hardware.escConfig, velocityGains,
                    hardware.conversions.nativeConversion.native(speed))
            )
        } else linePosition?.let { lineSnapshot ->
            val targetA = atan((targetLinePosition ?: 0.Inch) / lineScanner.lookAhead)
            val currentA = atan(lineSnapshot / lineScanner.lookAhead)
            val errorA = -(targetA - currentA)

            val (targetL, targetR) = uni.speedTargetAngleError(speed, errorA)

            val nativeL = hardware.conversions.nativeConversion.native(targetL)
            val nativeR = hardware.conversions.nativeConversion.native(targetR)

            TwoSided(
                    VelocityOutput(hardware.escConfig, velocityGains, nativeL),
                    VelocityOutput(hardware.escConfig, velocityGains, nativeR)
            )
        }
    }
}