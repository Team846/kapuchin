package com.lynbrookrobotics.kapuchin.control.math

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

fun theta(sl: Length, sr: Length, track: Length) = (sl - sr) / track * Radian
fun s(sl: Length, sr: Length) = avg(sl, sr)

class SimpleVectorTracking(
        private var trackLength: Length,
        init: Position
) : (Length, Length, Angle?) -> Unit {

    var x = init.x
    var y = init.y
    var bearing = init.bearing

    override fun invoke(sl: Length, sr: Length, externalBearing: Angle?) {
        val s = s(sl, sr)

        x += s * sin(bearing)
        y += s * cos(bearing)
        if (externalBearing == null) {
            bearing += theta(sl, sr, trackLength)
        } else {
            bearing = externalBearing
        }
    }
}

class RotationMatrixTracking(
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
            (RotationMatrix(init.bearing).rotate(it)) + init.vector
        }.let { (leftX, leftY) ->
            this.leftX = leftX
            this.leftY = leftY
        }

        UomVector(trackLength / 2, 0.Foot).let {
            (RotationMatrix(init.bearing).rotate(it)) + init.vector
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

        leftX = leftMatrix.rotateX(olpxMorpx, olpyMorpy) + prevRightX
        leftY = leftMatrix.rotateY(olpxMorpx, olpyMorpy) + prevRightY

        rightX = rightMatrix.rotateX(-olpxMorpx, -olpyMorpy) + prevLeftX
        rightY = rightMatrix.rotateY(-olpxMorpx, -olpyMorpy) + prevLeftY

        x = avg(leftX, rightX)
        y = avg(leftY, rightY)
        bearing += leftTheta + rightTheta
    }
}