package com.lynbrookrobotics.kapuchin.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * Generates a [Trajectory] given a [Path].
 *
 * 1) Going left to right (forward in time), find a [Trajectory] capping acceleration.
 * 2) Going right to left (reverse in time), find a [Trajectory] capping deceleration.
 * 3) Merge the two trajectories by always taking the smaller velocity at each [Segment].
 *
 * (any variable starting with "d" is a finite change Δ, not an infinitely small calculus "d")
 *
 * @author Andy
 *
 * @param path a path consisting of a list of waypoints.
 * @param maxVelocity the maximum linear velocity of the robot.
 * @param maxOmega the maximum angular velocity of the robot.
 * @param maxAcceleration the maximum linear acceleration of the robot.
 *
 * @return a trajectory consisting of a list of segments.
 */
fun pathToTrajectory(
        path: Path,
        maxVelocity: Velocity,
        maxOmega: AngularVelocity,
        maxAcceleration: Acceleration
): Trajectory {

    check(maxVelocity > 0.Metre / Second)
    check(maxAcceleration > 0.Metre / Second / Second)
    check(maxOmega > 0.Radian / Second)

    // (1)

    val forwardPath = path.toMutableList()
    val forwardTrajectory = oneWayAccelCap(forwardPath, maxVelocity, maxOmega, maxAcceleration)


    // (2)

    val reversePath = path.toMutableList().reversed()
    val reverseTrajectory = oneWayAccelCap(reversePath, maxVelocity, maxOmega, maxAcceleration)
            .reversed()
            .map { it.copy(omega = -it.omega) }


    // (3)

    val mergedTrajectory = mutableListOf<Segment>()
    for (i in 0 until path.size) {
        val f = forwardTrajectory[i]
        val r = reverseTrajectory[i]
        mergedTrajectory += if (f.velocity <= r.velocity) f else r
    }

    return mergedTrajectory
}

/**
 * Generates a [Trajectory] with acceleration capped in 1 direction (left to right).
 *
 * 1) Find the velocity cap of each [Segment] by using region of feasibility.
 *    https://www.desmos.com/calculator/qcpfvixfvg
 * 2) Going from left to right, cap linear acceleration.
 *
 * @author Andy
 *
 * @param path a path consisting of a list of waypoints.
 * @param maxVelocity the maximum linear velocity of the robot.
 * @param maxOmega the maximum angular velocity of the robot.
 * @param maxAcceleration the maximum linear acceleration of the robot.
 *
 * @return a trajectory without timestamps generated yet.
 */
private fun oneWayAccelCap(
        path: Path,
        maxVelocity: Velocity,
        maxOmega: AngularVelocity,
        maxAcceleration: Acceleration
): Trajectory {

    val trajectory = mutableListOf(
            Segment(path[0]),
            Segment(path[1], velocity = maxVelocity)
    )
    val dthetas = mutableListOf(0.Degree, 0.Degree)

    // (1)

    for (i in 2 until path.size) {
        val p1 = path[i - 2]
        val p2 = path[i - 1]
        val p3 = path[i] // current

        val d1 = distance(p1, p2)
        val dx = distance(p2, p3)
        val d3 = distance(p1, p3)

        // Use law of cosines to find Δtheta.
        val dtheta = {
            val mag = 180.Degree - acos((d1 * d1 + dx * dx - d3 * d3) / (d1 * dx * 2))
            val dir = -((p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x)).signum
            (mag * dir).takeUnless { dir == 0.0 } ?: 0.Degree
        }()

        dthetas += dtheta

        // Find Δt based on region of feasibility.
        val dt = abs(dtheta / maxOmega) + dx / maxVelocity

        trajectory += Segment(path[i], dx / dt)
    }


    // (2)

    for (i in 1 until path.size) {
        val s1 = trajectory[i - 1]
        val s2 = trajectory[i] // current
        val s3 = if (i != path.size - 1) trajectory[i + 1] else Segment(Waypt(0.Metre, 0.Metre))

        // Find Δt based on the target velocity and Δx.
        // The target velocity is the minimum velocity cap of either the current or the next segment.
        // The area under the segment in a velocity vs. time graph must equal Δx.
        val dx = distance(s2.waypt, s1.waypt)
        var dt = dx / (s1.velocity + (min(s2.velocity, s3.velocity) - s1.velocity) / 2)

        s2.velocity = min(s2.velocity, s3.velocity)
        s2.omega = dthetas[i] / dt

        // Cap linear acceleration
        if ((s2.velocity - s1.velocity) / dt > maxAcceleration) {
            // Find a new dt using acceleration and dx instead of a target velocity.
            // Δx = v₀t + (1/2)at²
            // t = (-v₀ ± sqrt(v₀² + 2aΔx)) / a -- quadratic formula
            // t = (-v₀ + sqrt(v₀² + 2aΔx)) / a -- only need to consider positive t
            dt = (-s1.velocity + v(maxAcceleration, s1.velocity, dx)) / maxAcceleration

            s2.velocity = s1.velocity + maxAcceleration * dt
            s2.omega = dthetas[i] / dt
        }
    }

    return trajectory
}