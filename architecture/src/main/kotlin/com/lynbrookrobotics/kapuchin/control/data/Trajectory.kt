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
 * @property omega the constant angular velocity throughout the segment.
 * @property time the amount of time elapsed since beginning of trajectory.
 */
data class Segment(
        var waypt: Waypt,
        var velocity: Velocity = 0.Foot / Second,
        var omega: AngularVelocity = 0.Radian / Second
)
