package com.lynbrookrobotics.kapuchin.control.math

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.avg
import info.kunalsheth.units.math.cos
import info.kunalsheth.units.math.sin

private fun theta(sl: Length, sr: Length, track: Length) = (sl - sr) / track * Radian
private fun s(sl: Length, sr: Length) = avg(sl, sr)

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
                x = s * cos(pos.bearing),
                y = s * sin(pos.bearing),
                bearing = theta
        )

        return pos
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
