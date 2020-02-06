package com.lynbrookrobotics.kapuchin.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*

/**
 * Given a differential drivetrain and a [Trajectory], calculate the left and right velocity outputs.
 *
 * @author Andy, Alvyn
 *
 * @property drivetrain a tank drive drivetrain component.
 * @property tolerance the tolerance to move onto the next waypoint.
 * @property endTolerance the tolerance to end at the final waypoint.
 * @param scope sensor scope of the routine.
 * @param trajectory the trajectory to follow.
 * @param origin the starting position of the robot.
 */
class TrajectoryFollower(
        private val drivetrain: GenericDrivetrainComponent,
        private val tolerance: Length,
        private val endTolerance: Length,
        scope: BoundSensorScope,
        trajectory: Trajectory,
        origin: Position
) {

    // Make waypoints relative to origin
    private val waypoints = with(RotationMatrix(origin.bearing)) {
        trajectory
                .map { (t, waypoint) -> rotate(waypoint) + origin.vector stampWith t }
                .iterator()
    }

    private var target = waypoints.next()
    private var done = false

    private var speed = 0.Foot / Second

    private val uni = UnicycleDrive(drivetrain, scope)
    private val position by with(scope) { drivetrain.hardware.position.readOnTick.withoutStamps }

    /**
     * Calculate the next left and right velocity outputs given the current position.
     *
     * For each side, the velocity is the current segment's linear velocity factored in with a PD loop for angular
     * velocity.
     *
     * @return the left and right velocities, null if the robot is at the target.
     */
    operator fun invoke(): TwoSided<Velocity>? {
        if (!waypoints.hasNext() && distance(position.vector, target.y) < endTolerance) {
            done = true
        } else if (waypoints.hasNext() && distance(position.vector, target.y) < tolerance) {
            val newTarget = waypoints.next()
            val dist = distance(newTarget.y, target.y)
            speed = dist / (newTarget.x - target.x)

            target = newTarget
        }

        val targetA = (target.y - position.vector).bearing
        val (velocityL, velocityR) = uni.speedAngleTarget(
                speed,
                targetA
        ).first

        return TwoSided(velocityL, velocityR).takeIf { !done }
    }
}