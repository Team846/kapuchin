package com.lynbrookrobotics.kapuchin.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * An x and y location.
 */
typealias Waypoint = UomVector<Length>

/**
 *  A list of [Waypoint]s for a robot to follow, with no information about speed/omega/timestmaps.
 */
typealias Path = List<Waypoint>

/**
 * a [Path] with timestamps at each [Waypoint].
 */
typealias Trajectory = List<TimeStamped<Waypoint>>

/**
 * Generates a [Trajectory] given a [Path].
 *
 * 1) Going left to right (forward in time), find a [Trajectory] capping acceleration.
 * 2) Going right to left (reverse in time), find a [Trajectory] capping deceleration.
 * 3) Merge the two trajectories by always taking the smaller velocity.
 *
 * (any variable starting with "d" is a finite change Δ, not an infinitely small calculus "d")
 *
 * @author Andy
 *
 * @param path a path consisting of a list of [Waypoint]s.
 * @param maxVelocity the maximum linear velocity of the robot.
 * @param maxOmega the maximum angular velocity of the robot.
 * @param maxAcceleration the maximum linear acceleration of the robot.
 *
 * @return a trajectory containing [Waypoint]s with timestamps.
 */
fun pathToTrajectory(
        path: Path,
        maxVelocity: Velocity,
        maxOmega: AngularVelocity,
        maxAcceleration: Acceleration
): Trajectory {

    check(maxVelocity > 0.Foot / Second)
    check(maxAcceleration > 0.Foot / Second / Second)
    check(maxOmega > 0.Radian / Second)

    // (1)

    val forwardPath = path.toMutableList()
    val forwardSegments = oneWayAccelCap(forwardPath, maxVelocity, maxOmega, maxAcceleration)


    // (2)

    val reversePath = path.toMutableList().reversed()
    val reverseSegments = oneWayAccelCap(reversePath, maxVelocity, maxOmega, maxAcceleration).reversed()


    // (3)

    val mergedTrajectory = mutableListOf(path.first() stampWith 0.Second)
    var totalT = 0.Second
    var prevV = 0.Foot / Second
    for (i in 1 until path.size) {
        val f = forwardSegments[i]
        val r = reverseSegments[i]
        val betterSegment = if (f.second <= r.second) f else r

        /*
        Theoretically a segment can have 0 velocity since in generation we assume a segment has constant acceleration,
        but our following code assumes constant velocity so if the segment's velocity is 0, just set it to the previous.
         */
        totalT += if (betterSegment.second == 0.Foot / Second)
            distance(path[i], path[i - 1]) / prevV
        else
            distance(path[i], path[i - 1]) / betterSegment.second


        prevV = betterSegment.second
        mergedTrajectory += betterSegment.first stampWith totalT
    }

    return mergedTrajectory
}

/**
 * Generates a [Trajectory] with acceleration capped in 1 direction (left to right).
 *
 * 1) Find the velocity cap of each segment by using region of feasibility.
 *    https://www.desmos.com/calculator/qcpfvixfvg
 * 2) Going from left to right, cap linear acceleration.
 *
 * @author Andy
 *
 * @param path a path consisting of a list of [Waypoint]s.
 * @param maxVelocity the maximum linear velocity of the robot.
 * @param maxOmega the maximum angular velocity of the robot.
 * @param maxAcceleration the maximum linear acceleration of the robot.
 *
 * @return a list of [Waypoint]s along with their respective velocities.
 */
private fun oneWayAccelCap(
        path: Path,
        maxVelocity: Velocity,
        maxOmega: AngularVelocity,
        maxAcceleration: Acceleration
): List<Pair<Waypoint, Velocity>> {

    // First point always as 0 velocity and the second point always has max velocity.
    val velocities = mutableListOf(0.Foot / Second, maxVelocity)

    // (1)

    for (i in 2 until path.size) {
        val p1 = path[i - 2]
        val p2 = path[i - 1]
        val p3 = path[i] // current

        val dx = distance(p2, p3)

        // Angle between p3 and the line formed by p1 and p2
        val dtheta = -((p2 - p1).bearing `coterminal -` (p3 - p2).bearing)

        // Find Δt based on region of feasibility.
        val dt = abs(dtheta / maxOmega) + dx / maxVelocity

        velocities += dx / dt
    }


    // (2)

    for (i in 1 until path.size) {
        // Find Δt based on the target velocity and Δx.
        // The target velocity is the minimum velocity cap of either the current or the next segment.
        // (if we are at the last waypoint, the "next segment" has 0 velocity.
        // The area under the segment in a velocity vs. time graph must equal Δx.
        val dx = distance(path[i], path[i - 1])
        val minVelocity = if (i != path.size - 1) min(velocities[i], velocities[i + 1]) else 0.Foot / Second
        var dt = dx / (velocities[i - 1] + (minVelocity - velocities[i - 1]) / 2)

        velocities[i] = dx / dt

        // Cap linear acceleration
        val currentMaxAcceleration = maxAcceleration - (velocities[i - 1] / maxVelocity) * maxAcceleration
        if ((velocities[i] - velocities[i - 1]) / dt > currentMaxAcceleration) {
            // Find a new dt using acceleration and dx instead of a target velocity.

            dt = if (currentMaxAcceleration == 0.Foot / Second / Second) {
                // Δx = v₀t
                dx / velocities[i - 1]
            } else {
                // Δx = v₀t + (1/2)at²
                // t = (-v₀ ± sqrt(v₀² + 2aΔx)) / a -- quadratic formula
                // t = (-v₀ + sqrt(v₀² + 2aΔx)) / a -- only need to consider positive t
                (-velocities[i - 1] + v(currentMaxAcceleration, velocities[i - 1], dx)) / currentMaxAcceleration
            }

            velocities[i] = velocities[i - 1] + currentMaxAcceleration * dt
        }
    }

    return path.zip(velocities) { p, v -> p to v }
}