package com.lynbrookrobotics.kapuchin.control.math

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

fun theta(sl: Length, sr: Length, track: Length) = (sl - sr) / track * Radian
fun s(sl: Length, sr: Length) = avg(sl, sr)

// todo: unit test!
fun simpleVectorTracking(
        trackLength: Length, init: Position
): (Length, Length) -> Position {

    var pos = init

    return fun(
            sl: Length, sr: Length
    ): Position {
        val s = s(sl, sr)
        val theta = theta(sl, sr, trackLength)

        pos += Position(
                //Using compass bearings not trig bearings
                x = s * sin(pos.bearing),
                y = s * cos(pos.bearing),
                bearing = theta
        )

        return pos
    }
}

class RotationMatrixTracking(
        private var trackLength: Length, init: Position, private var cache: Map<Angle, RotationMatrix> = emptyMap()
) : (Length, Length) -> Unit {

    private var lpx: Length
    private var lpy: Length
    private var rpx: Length
    private var rpy: Length

    init {
        UomVector(-trackLength / 2, 0.Foot).let {
            (RotationMatrix(init.bearing) rz it) + init.vector
        }.let { (lpx, lpy) ->
            this.lpx = lpx
            this.lpy = lpy
        }

        UomVector(trackLength / 2, 0.Foot).let {
            (RotationMatrix(init.bearing) rz it) + init.vector
        }.let { (rpx, rpy) ->
            this.rpx = rpx
            this.rpy = rpy
        }
    }

    var x = init.x
    var y = init.y
    var bearing = init.bearing

    override fun invoke(sl: Length, sr: Length) {
        val tl = theta(sl, 0.Foot, trackLength)
        val tr = theta(0.Foot, sr, trackLength)

        val ml = cache[tl] ?: RotationMatrix(tl)//.also { println("Cache miss L") }
        val mr = cache[tr] ?: RotationMatrix(tr)//.also { println("Cache miss R") }

        // copy of previous state
        val olpx = lpx
        val olpy = lpy
        val orpx = rpx
        val orpy = rpy

        // do matrix operations without creating new objects
        val olpxMorpx = olpx - orpx
        val olpyMorpy = olpy - orpy

        lpx = ml.rzComponentX(olpxMorpx, olpyMorpy) + orpx
        lpy = ml.rzComponentY(olpxMorpx, olpyMorpy) + orpy

        rpx = mr.rzComponentX(-olpxMorpx, -olpyMorpy) + olpx
        rpy = mr.rzComponentY(-olpxMorpx, -olpyMorpy) + olpy

        x = avg(lpx, rpx)
        y = avg(lpy, rpy)
        bearing += theta(sl, sr, trackLength)
    }
}