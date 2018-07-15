package com.lynbrookrobotics.kapuchin.control.math

import info.kunalsheth.units.generated.Angle
import info.kunalsheth.units.generated.Length

data class Waypoint(val x: Length, val y: Length, val theta: Angle)

data class CircularSCurve(val r: Length, val theta1: Angle, val theta2: Angle)

fun arcPath(p1: Waypoint, p2: Waypoint): CircularSCurve {
    TODO("Hanting knows how to do this...")
}