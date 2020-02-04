package com.lynbrookrobotics.kapuchin.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.Direction.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * The direction the drivetrain should follow a [Trajectory]
 *
 * @author Andy
 */
enum class Direction { Forward, Backward }

/**
 * Given a differential drivetrain and a [Trajectory], calculate the left and right velocity outputs.
 *
 * @author Andy, Alvyn
 *
 * @property drivetrain a tank drive drivetrain component.
 * @property tolerance the tolerance to move onto the next waypoint.
 * @property endTolerance the tolerance to end at the final waypoint.
 * @property direction the direction the drivetrain should follow the trajectory.
 * @param scope sensor scope of the routine.
 * @param trajectory the trajectory to follow.
 * @param origin the starting position of the robot.
 */
class TrajectoryFollower(
        private val drivetrain: GenericDrivetrainComponent,
        private val tolerance: Length,
        private val endTolerance: Length,
        private val direction: Direction,
        scope: BoundSensorScope,
        trajectory: Trajectory,
        origin: Position
) {

    // Make waypts relative to origin
    private var total = 0
    private var count = 0
    private var lastTarget: Waypt
    private val waypts = with(RotationMatrix(origin.bearing)) {
        trajectory
                .map { (t, waypt) -> rotate(waypt) + origin.vector stampWith t }
                .also {
                    total = it.size - 1
                    lastTarget = it.last().y
                }
                .iterator()
    }

    private var target = waypts.next()
    private var done = false

    private var speed = 0.Foot / Second

    private val uni = UnicycleDrive(drivetrain, scope)
    private val position by with(scope) { drivetrain.hardware.position.readOnTick.withoutStamps }

    /**
     * Bearing angle of target waypoint from current waypoint
     */
    private fun target(current: Waypt, target: Waypt) = atan2(target.x - current.x, target.y - current.y)

    /**
     * Calculate the next left and right velocity outputs given the current position.
     *
     * For each side, the velocity is the current segment's linear velocity factored in with a PD loop for angular
     * velocity.
     *
     * @return the left and right velocities, null if the robot is at the target.
     */
    operator fun invoke(): TwoSided<Velocity>? {
        if (!waypts.hasNext() && distance(position.vector, lastTarget) < endTolerance) {
            println("-------------------------------------------------------")
            println("CURRENT: ${position.vector}")
            println("LAST: ${lastTarget}")
            println("DISTANCE: ${distance(position.vector, lastTarget)}")
            done = true
        } else if (waypts.hasNext() && distance(position.vector, target.y) < tolerance) {
            val newTarget = waypts.next()
            val dist = distance(newTarget.y, target.y)
            speed = dist / (newTarget.x - target.x)

            if (speed < 1.Foot / Second || !speed.siValue.isFinite()) {
                speed = 1.Foot / Second
            }
            target = newTarget
        }

        val targetA = target(position.vector, target.y)
        var (velocityL, velocityR) = uni.speedAngleTarget(
                speed,
                targetA
        ).first

        if (direction == Backward) {
            // negate and switch left and right
            velocityL = -velocityR.also { velocityR = -velocityL }
        }
        return TwoSided(velocityL, velocityR).takeIf { !done }
    }
}