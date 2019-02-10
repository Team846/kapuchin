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

fun rotationMatrixTracking(
        trackLength: Length, init: Position, cache: Map<Angle, RotationMatrix> = emptyMap()
) : (Length, Length) -> Position {

    var leftPos = UomVector(-trackLength / 2, 0.Foot).let {
        (RotationMatrix(init.bearing) rz it) + init.vector
    }
    var rightPos = UomVector(trackLength / 2, 0.Foot).let {
        (RotationMatrix(init.bearing) rz it) + init.vector
    }

    var lastBearing = init.bearing

    fun RotationMatrix.rzAbout(origin: UomVector<Length>, that: UomVector<Length>) = (this rz (that - origin)) + origin

    return fun(sl: Length, sr: Length): Position {
        val tl = theta(sl, 0.Foot, trackLength)
        val tr = theta(0.Foot, sr, trackLength)

        val ml = cache[tl] ?: RotationMatrix(tl)//.also { println("Cache miss L") }
        val mr = cache[tr] ?: RotationMatrix(tr)//.also { println("Cache miss R") }

        leftPos = ml.rzAbout(rightPos, leftPos)
        rightPos = mr.rzAbout(leftPos, rightPos)

        lastBearing += theta(sl, sr, trackLength)
        return Position(avg(leftPos.x, rightPos.x), avg(leftPos.y, rightPos.y), lastBearing)
    }
}

//class RotationMatrixTracking(
//        empiricalThetaPerTick: TwoSided<Angle>, trackLength: Length, init: Position
//) : (Sequence<RotationMatrixTracking.Ticks>, Angle) -> Position {
//
//    enum class Ticks {
//        LeftForward, RightForward,
//        LeftBackward, RightBackward
//    }
//
//    private var pos = TwoSided(
//            UomVector(-trackLength / 2, 0.Foot),
//            UomVector(trackLength / 2, 0.Foot)
//    ).run {
//        val mtrx = RotationMatrix(init.bearing)
//
//        TwoSided(
//                mtrx rz left + init.vector,
//                mtrx rz right + init.vector
//        )
//    }
//
//    private val lf = RotationMatrix(empiricalThetaPerTick.left)
//    private val lb = RotationMatrix(-empiricalThetaPerTick.left)
//    private val rf = RotationMatrix(empiricalThetaPerTick.right)
//    private val rb = RotationMatrix(-empiricalThetaPerTick.right)
//
//    fun RotationMatrix.rzAbout(that: UomVector<Length>, origin: UomVector<Length>) = (this rz (that - origin)) + origin
//
//    override operator fun invoke(
//            feedback: Sequence<Ticks>,
//            finalBearing: Angle
//    ): Position {
//        pos = feedback.fold(pos) { acc, tick ->
//            when (tick) {
//                Ticks.LeftForward -> lf.rzAbout(acc.left, acc.right)
//                Ticks.RightForward -> rf.rzAbout(acc.left, acc.right)
//                Ticks.LeftBackward -> lb.rzAbout(acc.left, acc.right)
//                Ticks.RightBackward -> lf.rzAbout(acc.left, acc.right)
//            }
//        }.copy(bearing = finalBearing)
//
//        return pos
//    }
//}
