package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
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
                val (x, y) = startingRot.rotate(loc.vector - startingLoc)

                if (distance(loc.vector, last.vector) > ptDistance) {
                    it.println("${t.Second}\t${x.Foot}\t${y.Foot}")
                    last = loc
                }

                delay(100.milli(Second))
            }

            val (t, loc) = pos
            val (x, y) = startingRot.rotate(loc.vector - startingLoc)
            it.println("${t.Second}\t${x.Foot}\t${y.Foot}")
        }
    }
}

suspend fun DrivetrainComponent.followTrajectory(
        trajectory: Trajectory,
        tolerance: Length,
        endTolerance: Length,
        origin: Position = hardware.position.optimizedRead(currentTime, 0.Second).y
) = startRoutine("Read Journal") {

    val follower = TrajectoryFollower(
        this@followTrajectory, tolerance, endTolerance, this@startRoutine, trajectory, origin
    )

    controller {
        val velocities = follower()
        if (velocities != null) {
            val nativeL = hardware.conversions.nativeConversion.native(velocities.left)
            val nativeR = hardware.conversions.nativeConversion.native(velocities.right)
            TwoSided(
                    VelocityOutput(hardware.escConfig, velocityGains, nativeL),
                    VelocityOutput(hardware.escConfig, velocityGains, nativeR)
            )
        } else {
            null
        }
    }
}

suspend fun DrivetrainComponent.waypoint(motionProfile: (Length) -> Velocity, target: UomVector<Length>, tolerance: Length) = startRoutine("Waypoint") {
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