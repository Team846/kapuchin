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
 */
data class Segment(
        val waypt: Waypt,
        val velocity: Velocity,
        val omega: AngularVelocity
)

/**
 * Internal representation of a segment with mutable properties.
 */
internal data class MutableSegment(
        val waypt: Waypt,
        var velocity: Velocity = 0.Foot / Second,
        var omega: AngularVelocity = 0.Radian / Second
)