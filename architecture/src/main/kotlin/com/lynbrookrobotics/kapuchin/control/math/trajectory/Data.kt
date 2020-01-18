package com.lynbrookrobotics.kapuchin.control.math.trajectory

import com.lynbrookrobotics.kapuchin.control.data.*
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
 * @property time the amount of time elapsed since beginning of trajectory.
 * @property velocity the final velocity at the end of the segment.
 * @property acceleration the constant acceleration throughout the segment.
 */
data class Segment(
        val waypt: Waypt,
        var velocity: Velocity = 0.Metre / Second,
        var acceleration: Acceleration = 0.Metre / Second / Second,
        var time: Time = 0.Second
)
