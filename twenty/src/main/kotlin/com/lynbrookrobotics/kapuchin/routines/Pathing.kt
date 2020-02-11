package com.lynbrookrobotics.kapuchin.routines

import com.ctre.phoenix.motorcontrol.NeutralMode.Brake
import com.ctre.phoenix.motorcontrol.NeutralMode.Coast
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

suspend fun journal(dt: DrivetrainHardware, ptDistance: Length = 3.Inch) = startChoreo("Journal") {

    val pos by dt.position.readEagerly(2.milli(Second)).withoutStamps
    val log = File("/home/lvuser/journal.tsv").printWriter().also {
        it.println("x\ty")
        it.println("0.0\t0.0")
    }

    val startingLoc = pos.vector
    val startingRot = RotationMatrix(-pos.bearing)

    var last = pos

    val drivetrainEscs = setOf(dt.leftMasterEsc, dt.rightMasterEsc, dt.leftSlaveEsc, dt.rightSlaveEsc)

    choreography {
        try {
            drivetrainEscs.forEach { it.setNeutralMode(Coast) }
            while (isActive) {
                val (x, y) = startingRot rz (pos.vector - startingLoc)

                if (distance(pos.vector, last.vector) > ptDistance) {
                    log.println("${x.Foot}\t${y.Foot}")
                    last = pos
                }

                delay(50.milli(Second))
            }
        } finally {
            val (x, y) = startingRot rz (pos.vector - startingLoc)
            log.println("${x.Foot}\t${y.Foot}")
            log.close()

            drivetrainEscs.forEach { it.setNeutralMode(Brake) }
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
            val nativeL = hardware.conversions.encoder.left.native(velocities.left)
            val nativeR = hardware.conversions.encoder.right.native(velocities.right)
            TwoSided(
                    VelocityOutput(hardware.escConfig, velocityGains.left, nativeL),
                    VelocityOutput(hardware.escConfig, velocityGains.right, nativeR)
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

        val targetA = (target - location).bearing
        val speed = motionProfile(distance)
        val (targVels, _) = uni.speedTargetAngleTarget(speed, targetA)

        val nativeL = hardware.conversions.encoder.left.native(targVels.left)
        val nativeR = hardware.conversions.encoder.right.native(targVels.right)

        TwoSided(
                VelocityOutput(hardware.escConfig, velocityGains.left, nativeL),
                VelocityOutput(hardware.escConfig, velocityGains.right, nativeR)
        ).takeUnless {
            distance < tolerance
        }
    }
}