package com.lynbrookrobotics.kapuchin.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * Given a differential drivetrain and a [Trajectory], calculate the left and right velocities at a specific time.
 *
 * @author Andy, Alvyn
 *
 * @property drivetrain a tank drive drivetrain component.
 * @property tolerance the tolerance to move onto the next waypoint.
 * @property endTolerance the tolerance to end at the final waypoint.
 * @scope sensor scope of the routine.
 * @param trajectory the trajectory to follow.
 * @param origin the starting position of the robot.
 */
class TrajectoryFollower(
        val drivetrain: GenericDrivetrainComponent,
        val tolerance: Length,
        val endTolerance: Length,
        scope: BoundSensorScope,
        trajectory: Trajectory,
        origin: Position
) {

    // Make segment waypoints relative to origin
    private val segments = with(RotationMatrix(origin.bearing)) {
        trajectory
                .also { t -> t.forEach { it.waypt = rotate(it.waypt) + origin.vector } }
                .drop(1)
                .iterator()
    }

    private var target = segments.next()
    private var done = false

    private val uni = UnicycleDrive(drivetrain, scope)
    private val position by with(scope) { drivetrain.hardware.position.readOnTick.withoutStamps }

    operator fun invoke(): TwoSided<Velocity>? {
        val distanceToNext = distance(position.vector, target.waypt)

        done = !segments.hasNext() && distanceToNext < endTolerance
        if (segments.hasNext() && distanceToNext < tolerance) {
            target = segments.next()
        }

        // trig angle to compass bearing
        val targetAngle = 90.Degree - atan2(target.waypt.x - position.vector.x, target.waypt.y - position.vector.y)

        with(drivetrain.hardware.conversions) {
            var (velocityL, velocityR) = uni.speedAngleTarget(target.velocity, targetAngle).first
            val feedforward = ((target.omega / Radian) * trackLength) / 2
            velocityL -= feedforward
            velocityR += feedforward

            return TwoSided(velocityL, velocityR).takeIf { !done }
        }
    }
}