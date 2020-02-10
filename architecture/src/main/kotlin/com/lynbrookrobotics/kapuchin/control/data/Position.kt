package com.lynbrookrobotics.kapuchin.control.data

import com.lynbrookrobotics.kapuchin.logging.*
import info.kunalsheth.units.generated.*

/**
 * Represents a robot location
 *
 * @author Kunal
 *
 * @property x robot cartesian coordinate
 * @property y robot cartesian coordinate
 * @property bearing robot bearing
 */
data class Position(val x: Length, val y: Length, val bearing: Angle) {

    operator fun plus(that: Position) = Position(
            this.x + that.x,
            this.y + that.y,
            this.bearing + that.bearing
    )

    operator fun minus(that: Position) = Position(
            this.x - that.x,
            this.y - that.y,
            this.bearing - that.bearing
    )

    val vector get() = UomVector(x, y)

    override fun toString() =
            "(x, y, bearing) = (${x.Foot withDecimals 2}, ${y.Foot withDecimals 2}, ${bearing.Degree withDecimals 0}˚)"
}