package com.lynbrookrobotics.kapuchin.control.math

import com.lynbrookrobotics.kapuchin.control.data.Position
import com.lynbrookrobotics.kapuchin.control.data.TwoSided
import com.lynbrookrobotics.kapuchin.control.data.plus
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.avg
import info.kunalsheth.units.math.cos
import info.kunalsheth.units.math.sin

private fun theta(sl: Length, sr: Length, track: Length) = (sl - sr) / track * Radian
private fun s(sl: Length, sr: Length) = avg(sl, sr)

fun simpleVectorTracking(
        trackLength: Length, init: Position
): (Sequence<TwoSided<Length>>, Angle) -> Position {

    var pos = init

    return fun(
            feedback: Sequence<TwoSided<Length>>,
            finalBearing: Angle
    ): Position {
        pos = feedback.fold(pos) { acc, (sl, sr) ->
            val s = s(sl, sr)
            val theta = theta(sl, sr, trackLength)

            acc + Position(
                    x = s * cos(acc.bearing),
                    y = s * sin(acc.bearing),
                    bearing = theta
            )
        }.copy(bearing = finalBearing)

        return pos
    }
}