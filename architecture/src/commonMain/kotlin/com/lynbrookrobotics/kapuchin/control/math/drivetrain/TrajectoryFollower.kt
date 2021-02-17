package com.lynbrookrobotics.kapuchin.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * Check if a [Waypoint] is behind the line that goes through a [Position] that is perpendicular to its bearing.
 *
 * 2 points of the line are generated by generating two points. The points are 1 foot to the left/right of the given
 * [Position]
 *
 * https://math.stackexchange.com/questions/274712/calculate-on-which-side-of-a-straight-line-is-a-given-point-located
 *
 * @receiver the [Waypoint] to check.
 * @param pos the [Position] which forms the line.
 *
 * @author Andy
 */
internal infix fun Waypoint.isBehind(pos: Position): Boolean {
    fun d(target: Waypoint, p1: Waypoint, p2: Waypoint) =
        ((target.x - p1.x) * (p2.y - p1.y) - (target.y - p1.y) * (p2.x - p1.x)).signum

    // p1 is 1 foot from pos the 90 deg CCW direction
    // p2 is 1 foot from pos the 90 deg CW direction
    val p1 = Waypoint(pos.x + 1.Foot * sin(pos.bearing - 90.Degree), pos.y + 1.Foot * cos(pos.bearing - 90.Degree))
    val p2 = Waypoint(pos.x + 1.Foot * sin(pos.bearing + 90.Degree), pos.y + 1.Foot * cos(pos.bearing + 90.Degree))

    // p3 is 1 foot from pos in its bearing to test for initial D value
    val p3 = Waypoint(pos.x + 1.Foot * sin(pos.bearing), pos.y + 1.Foot * cos(pos.bearing))

    return d(p3, p1, p2) != d(this, p1, p2)
}

/**
 * Extrapolate a [Waypoint] in the direction of the line formed by the [Waypoint] and another given [Waypoint] by a
 * specified distance.
 *
 * @receiver the [Waypoint] (timestamped for convenience when used in [TrajectoryFollower]) to extrapolate from.
 * @param from another [Waypoint] used as reference for the direction of extrapolation.
 * @param by the extrapolation distance.
 * @return the resulting [Waypoint] (timestamped for convenience when used in [TrajectoryFollower]).
 *
 * @author Andy
 */
internal fun TimeStamped<Waypoint>.extrapolate(from: Waypoint, by: Length): TimeStamped<Waypoint> {
    val bearing = atan2(y.x - from.x, y.y - from.y)
    return Waypoint(y.x + by * sin(bearing), y.y + by * cos(bearing)) stampWith x
}

/**
 * Given a differential drivetrain, follow a [Trajectory] by constantly calculating left and right velocity outputs.
 *
 * The robot will follow the [Waypoint]s with a linear velocity specified by the trajectory, and an angular velocity
 * calculated with a PID loop given the error between the current bearing and the bearing of a target [Waypoint].
 *
 * Given a target [Waypoint], the robot will aim to steer towards a point extrapolated from the target and the previous
 * [Waypoint]. A shorter extrapolation distance means the robot will steer much sharper, but may oscillate on straight
 * sections. The extrapolation distance is calculated as `(currentSpeed / maxSpeed) * maxExtrapolate` so that the robot
 * will steer sharper on turns (where the `currentSpeed` is lower.)
 *
 * As the robot travels, a [Waypoint] is considered "passed" and a new target will be set once the target [Waypoint] is
 * behind x-axis of the robot (the line perpendicular to the direction of its front). This means the robot may
 * accumulate lot's of error if it slips a lot on its path and can't correct it enough, but it'll always try to
 * finish the [Trajectory] without oscillating too hard or spinning in place. For safety, the robot will abort the
 * [Trajectory] if the error exceeds safetyTolerance.
 *
 * @param scope the sensor scope of the routine.
 * @param trajectory the [Trajectory] to follow.
 * @param origin the starting [Position] of the robot.
 * @property drivetrain the tank drive drivetrain component.
 * @property maxExtrapolate the maximum target [Waypoint] extrapolation distance (varies with current speed).
 * @property maxSpeed the maximum speed of the robot including constraints (e.g. a speed factor).
 * @property safetyTolerance the maximum error the robot can be from a [Waypoint] before ending the [Trajectory] early.
 * @property reverse whether or not the robot should run with the back going forwards.
 *
 * @author Andy, Alvyn
 */
class TrajectoryFollower(
    scope: BoundSensorScope,
    trajectory: Trajectory,
    origin: Position,
    private val drivetrain: GenericDrivetrainComponent,
    private val maxExtrapolate: Length,
    private val safetyTolerance: Length,
    private val reverse: Boolean,
) {

    // Make waypoints relative to origin
    private val waypoints = origin.bearing
        .let { if (reverse) 180.Degree `coterminal +` it else it }
        .let { RotationMatrix(it) }
        .let { mtrx ->
            trajectory
                .map { (t, waypoint) -> (mtrx rz waypoint) + origin.vector stampWith t }
                .iterator()
        }

    // The current target
    private var target = waypoints.next()

    // The current extrapolated target
    private var extrapolatedTarget = target

    // Whether or not the trajectory is complete
    private var done = false

    // The current target speed
    private var speed = 0.Foot / Second

    private val errors = mutableSetOf<Length>()

    private val uni = UnicycleDrive(drivetrain, scope)
    private val position by drivetrain.hardware.positionDelegate(scope)

    private fun finish() {
        done = true
        drivetrain.log(Debug) { "*****Finished Trajectory*****"}

        if (errors.size == 0) {
            drivetrain.log(Error) { "No error data points" }
            return
        }

        val avg = errors.sumOf { it.Inch }.Inch / errors.size
        val max = errors.maxByOrNull { it.Inch }!!
        drivetrain.log(Debug) { "Avg error: ${avg.Inch} in | Max error: ${max.Inch} in" }
    }

    /**
     * Calculate the next left and right velocity outputs given the current position.
     *
     * @return the left and right velocities, null if the robot is at the target.
     */
    operator fun invoke(): TwoSided<Velocity>? {
        val error = distance(position.y.vector, target.y)

        if (!waypoints.hasNext() && target.y isBehind position.y) {
            finish()
        } else if (waypoints.hasNext() && target.y isBehind position.y) {
            drivetrain.log(Debug) { "*****Hit Waypoint*****" }
            drivetrain.log(Debug) { "Current pos: ${position.y.x.Foot withDecimals 2} ft, ${position.y.y.Foot withDecimals 2} ft @${position.y.bearing.Degree withDecimals 0} deg" }

            // Check error
            drivetrain.log(Debug) { "Error: ${error.Inch withDecimals 2} in" }
            if (error > safetyTolerance) {
                drivetrain.log(Error) { "*****ABORTING TRAJECTORY*****" }
                drivetrain.log(Error) { "Error (${error.Inch} in) exceeds safety tolerance (${safetyTolerance.Inch} in)" }
                finish()
            }

            errors += error

            // Set new target and speed
            val newTarget = waypoints.next()
            speed = distance(newTarget.y, target.y) / (newTarget.x - target.x)
            extrapolatedTarget = newTarget.extrapolate(target.y, maxExtrapolate * (speed / drivetrain.maxSpeed))
            target = newTarget

            drivetrain.log(Debug) { "New target: ${newTarget.y.x.Foot withDecimals 2} ft, ${newTarget.y.y.Foot withDecimals 2} ft" }
            drivetrain.log(Debug) { "Extrap Dist: ${(maxExtrapolate * speed / drivetrain.maxSpeed).Inch} in (${speed / drivetrain.maxSpeed}%)" }
            drivetrain.log(Debug) { "Extrap Target: ${extrapolatedTarget.y.x.Foot withDecimals 2} ft, ${extrapolatedTarget.y.y.Foot withDecimals 2} ft" }
        }

        val targetA = (extrapolatedTarget.y - position.y.vector).bearing

        val (velocityL, velocityR) = uni.speedTargetAngleTarget(
            if (reverse) -speed else speed,
            if (reverse) 180.Degree `coterminal +` targetA else targetA
        ).first

        return TwoSided(velocityL, velocityR).takeIf { !done }
    }
}