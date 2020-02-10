package com.lynbrookrobotics.kapuchin.control.math

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

fun theta(sl: Length, sr: Length, track: Length) = (sl - sr) / track * Radian

class SimpleVectorTracking(
        private var trackLength: Length,
        init: Position
) : (Length, Length, Angle?) -> Unit {

    var x = init.x
    var y = init.y
    var bearing = init.bearing

    override fun invoke(sl: Length, sr: Length, externalBearing: Angle?) {
        val s = avg(sl, sr)

        if (externalBearing != null) bearing = externalBearing
        else bearing += theta(sl, sr, trackLength)

        x += s * sin(bearing)
        y += s * cos(bearing)
    }
}

class CircularArcTracking(
        init: Position
) : (Length, Length, Angle) -> Unit {

    var x = init.x
    var y = init.y
    var bearing = init.bearing

    override fun invoke(sl: Length, sr: Length, newBearing: Angle) {
        val `Δθ` = newBearing `coterminal -` bearing
        val s = avg(sl, sr)

        if (`Δθ` == 0.Degree) {
            x += s * sin(bearing)
            y += s * cos(bearing)
        } else {
            val r = s / `Δθ`.Radian

            val mtrx = RotationMatrix(`Δθ`)
            val ox = x + r * sin(bearing + 90.Degree)
            val oy = y + r * cos(bearing + 90.Degree)

            val `x - ox` = x - ox
            val `y - oy` = y - oy

            x = mtrx.rzCoordinateX(`x - ox`, `y - oy`) + ox
            y = mtrx.rzCoordinateY(`x - ox`, `y - oy`) + oy
        }

        bearing = newBearing
    }
}

class HighFrequencyTracking(
        private var trackLength: Length,
        init: Position,
        private var cache: Map<Angle, RotationMatrix> = emptyMap()
) : (Length, Length) -> Unit {

    private var leftX: Length
    private var leftY: Length
    private var rightX: Length
    private var rightY: Length

    var x = init.x
    var y = init.y
    var bearing = init.bearing

    init {
        UomVector(-trackLength / 2, 0.Foot).let {
            (RotationMatrix(init.bearing) rz it) + init.vector
        }.let { (leftX, leftY) ->
            this.leftX = leftX
            this.leftY = leftY
        }

        UomVector(trackLength / 2, 0.Foot).let {
            (RotationMatrix(init.bearing) rz it) + init.vector
        }.let { (rightX, rightY) ->
            this.rightX = rightX
            this.rightY = rightY
        }
    }

    override fun invoke(sl: Length, sr: Length) {
        val leftTheta = theta(sl, 0.Foot, trackLength)
        val rightTheta = theta(0.Foot, sr, trackLength)

        val leftMatrix = cache[leftTheta] ?: RotationMatrix(leftTheta)
        val rightMatrix = cache[rightTheta] ?: RotationMatrix(rightTheta)

        // copy of previous state
        val prevLeftX = leftX
        val prevLeftY = leftY
        val prevRightX = rightX
        val prevRightY = rightY

        // do matrix operations without creating new objects
        val olpxMorpx = prevLeftX - prevRightX
        val olpyMorpy = prevLeftY - prevRightY

        leftX = leftMatrix.rzCoordinateX(olpxMorpx, olpyMorpy) + prevRightX
        leftY = leftMatrix.rzCoordinateY(olpxMorpx, olpyMorpy) + prevRightY

        rightX = rightMatrix.rzCoordinateX(-olpxMorpx, -olpyMorpy) + prevLeftX
        rightY = rightMatrix.rzCoordinateY(-olpxMorpx, -olpyMorpy) + prevLeftY

        x = avg(leftX, rightX)
        y = avg(leftY, rightY)
        bearing += leftTheta + rightTheta
    }
}