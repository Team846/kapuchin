package com.lynbrookrobotics.kapuchin.control.data

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
// todo: unit test!
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

}