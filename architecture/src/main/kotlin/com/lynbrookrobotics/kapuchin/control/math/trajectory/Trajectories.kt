package com.lynbrookrobotics.kapuchin.control.math.trajectory

import com.lynbrookrobotics.kapuchin.control.math.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.sqrt

/**
 * Generates a [Trajectory] given a [Path].
 *
 * 1) Going left to right (forward in time), find a [Trajectory] capping acceleration.
 * 2) Going right to left (reverse in time), find a [Trajectory] capping deceleration.
 * 3) Merge the two trajectories by always taking the smaller velocity at each [Segment].
 * 4) Generate a timestamp and acceleration for each [Segment] of the merged [Trajectory].
 *
 * @author Andy
 *
 * @param waypts a path consisting of a list of waypoints.
 * @param maxV the maximum linear velocity of the robot.
 * @param maxOmega the maximum angular velocity of the robot.
 * @param maxA the maximum linear acceleration of the robot.
 *
 * @return a trajectory consisting of a list of segments.
 */
fun pathToTrajectory(
        waypts: Path,
        maxV: Velocity,
        maxOmega: AngularVelocity,
        maxA: Acceleration
): Trajectory {

    check(maxV > 0.Metre / Second)
    check(maxOmega > 0.Radian / Second)
    check(maxA > 0.Metre / Second / Second)

    // (1)

    val forwardWaypts = waypts.toMutableList()
    val forwardTrajectory = oneWayAccelCap(forwardWaypts, maxV, maxOmega, maxA)

    // (2)

    val reverseWaypts = waypts.toMutableList().reversed()
    var reverseTrajectory = oneWayAccelCap(reverseWaypts, maxV, maxOmega, maxA)

    // Reverse the times for the reverse trajectory so time 0 is the first waypoint
    val maxT = reverseTrajectory.last().time
    reverseTrajectory.forEach { it.time = maxT - it.time }
    reverseTrajectory = reverseTrajectory.reversed()

    // (3)

    val mergedTrajectory = mutableListOf<Segment>()
    for (i in 0 until waypts.size) {
        val f = forwardTrajectory[i]
        val r = reverseTrajectory[i]
        mergedTrajectory += if (f.velocity <= r.velocity) f else r
    }

    // (4)

    mergedTrajectory.first().time = 0.Second
    for (i in 1 until waypts.size) {
        val s1 = mergedTrajectory[i - 1]
        val s2 = mergedTrajectory[i]

        // Δx = ((v + v₀) / 2) * t
        // t = Δx /  ((v + v₀) / 2)
        val dt = distance(s1.waypt, s2.waypt) / ((s1.velocity + s2.velocity) / 2)
        mergedTrajectory[i].time = mergedTrajectory[i - 1].time + dt

        // v = v₀ + at
        // a = (v - v₀) / t
        mergedTrajectory[i].acceleration = (s2.velocity - s1.velocity) / dt
    }

    return mergedTrajectory
}

/**
 * Generates a [Trajectory] with acceleration capped in 1 direction (left to right).
 *
 * 1) Find the velocity cap of each [Segment] by using region of feasibility (higher Δtheta means lower velocity cap).
 * 2) Going from left to right, cap linear acceleration.
 * Timestamps are not generated yet.
 *
 * @author Andy
 *
 * @param waypts a path consisting of a list of waypoints.
 * @param maxV the maximum linear velocity of the robot.
 * @param maxOmega the maximum angular velocity of the robot.
 * @param maxA the maximum linear acceleration of the robot.
 *
 * @return a trajectory without timestamps generated yet.
 */
private fun oneWayAccelCap(
        waypts: Path,
        maxV: Velocity,
        maxOmega: AngularVelocity,
        maxA: Acceleration
): Trajectory {

    val trajectory = mutableListOf(
            Segment(waypts[0]),
            Segment(waypts[1], maxV)
    )

    // (1)

    for (i in 2 until waypts.size) {
        val p1 = waypts[i - 2]
        val p2 = waypts[i - 1]
        val p3 = waypts[i] // current

        val d1 = distance(p1, p2)
        val dx = distance(p2, p3)
        val d3 = distance(p1, p3)

        // Use law of cosines to find Δtheta
        val dtheta =
                if (d1 + dx == d3) 0.Degree
                else 180.Degree - acos((d1 * d1 + dx * dx - d3 * d3) / (d1 * dx * 2))

        // Find Δt based on region of feasibility
        val dt = abs(dtheta / maxOmega) + dx / maxV

        trajectory += Segment(waypts[i], dx / dt)
    }

    // (2)

    for (i in 1 until waypts.size) {
        val s1 = trajectory[i - 1]
        val s2 = trajectory[i] // current
        val s3 = if (i != waypts.size - 1) trajectory[i + 1] else Segment(Waypt(0.Metre, 0.Metre))

        // Find Δt based on the target velocity and Δx
        // The target velocity is the minimum velocity cap of either the current or the next segment.
        // The area under the segment in a velocity vs. time graph must equal Δx
        val dx = distance(s2.waypt, s1.waypt)
        var dt = dx / (s1.velocity + (min(s2.velocity, s3.velocity) - s1.velocity) / 2)

        s2.velocity = min(s2.velocity, s3.velocity)
        s2.acceleration = (s2.velocity - s1.velocity) / dt

        // Cap linear acceleration
        if (s2.acceleration > maxA) {
            // Find a new dt using acceleration and dx instead of a target velocity
            // Δx = v₀t + (1/2)at²
            // t = (-v₀ ± sqrt(v₀² + 2aΔx)) / a -- quadratic formula
            // t = (-v₀ + sqrt(v₀² + 2aΔx)) / a -- only need to consider positive t
            dt = (-s1.velocity + Velocity(sqrt((s1.velocity * s1.velocity + 2 * maxA * dx).siValue))) / maxA

            s2.velocity = s1.velocity + maxA * dt
            s2.acceleration = maxA
        }
    }

    return trajectory
}