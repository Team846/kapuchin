package com.lynbrookrobotics.kapuchin.control.data

import info.kunalsheth.units.generated.*

typealias Waypt = UomVector<Length> // x and y location
typealias Path = List<Waypt> // a list of points for a robot to follow
typealias Trajectory = List<Segment> // a path with generated timestamps, velocities, and acceleration

/**
 * Represents the segment between two waypoints in a path.
 *
 * @author Andy
 *
 * @property waypt the ending waypoint of the segment.
 * @property velocity the final linear velocity at the end of the segment.
 * @property omega the final angular velocity at the end of the segment.
 * @property acceleration the constant linear acceleration throughout the segment.
 * @property alpha the constant angular acceleration throughout the segment.
 * @property time the amount of time elapsed since beginning of trajectory.
 */
data class Segment(
        var waypt: Waypt,
        var velocity: Velocity = 0.Metre / Second,
        var omega: AngularVelocity = 0.Radian / Second,
        var acceleration: Acceleration = 0.Metre / Second / Second,
        var alpha: AngularAcceleration = 0.Radian / Second / Second,
        var time: Time = 0.Second
) {
    override fun toString(): String {
        return "${time.Second}\t" +
                "${waypt.x.Foot}\t" +
                "${waypt.y.Foot}\t" +
                "${velocity.FootPerSecond}\t" +
                "${omega / Radian * Second}\t" +
                "${acceleration.FootPerSecondSquared}\t" +
                "${alpha / Radian * Second * Second}"
    }
}
