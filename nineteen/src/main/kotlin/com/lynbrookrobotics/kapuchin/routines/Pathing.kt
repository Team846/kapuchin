package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.isActive
import java.io.File

fun target(current: Waypt, target: Waypt) = atan2(target.x - current.x, target.y - current.y)

suspend fun journal(dt: DrivetrainHardware, ptDistance: Length = 6.Inch) = startChoreo("Journal") {

    val pos by dt.position.readEagerly(2.milli(Second)).withStamps
    val log = File("/tmp/journal.tsv").printWriter().also {
        it.println("time\tx\ty")
    }

    val startingLoc = pos.y.vector
    val startingRot = RotationMatrix(-pos.y.bearing)

    var last = pos.y

    choreography {
        log.use {
            while (isActive) {
                val (t, loc) = pos
                val (x, y) = startingRot rz (loc.vector - startingLoc)

                if (distance(loc.vector, last.vector) > ptDistance) {
                    it.println("${t.Second}\t${x.Foot}\t${y.Foot}")
                    last = loc
                }

                delay(100.milli(Second))
            }

            val (t, loc) = pos
            val (x, y) = startingRot rz (loc.vector - startingLoc)
            it.println("${t.Second}\t${x.Foot}\t${y.Foot}")
        }
    }
}

suspend fun Drivetrain.followTrajectory(
        tolerance: Length, endTolerance: Length,
        deceleration: Acceleration, waypts: List<TimeStamped<Waypt>>,
        origin: Position = hardware.position.optimizedRead(currentTime, 0.Second).y
) = startRoutine("Read Journal") {
    val position by hardware.position.readOnTick.withStamps
    val uni = UnicycleDrive(this@followTrajectory, this@startRoutine)

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

suspend fun Drivetrain.waypoint(motionProfile: (Length) -> Velocity, target: UomVector<Length>, tolerance: Length) = startRoutine("Waypoint") {
    val position by hardware.position.readOnTick.withStamps
    val uni = UnicycleDrive(this@waypoint, this@startRoutine)

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