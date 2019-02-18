package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.networktables.NetworkTableInstance
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class LimelightHardware : RobotHardware<LimelightHardware>() {
    override val name = "Limelight"
    override val priority = Priority.Lowest

    private val mounting by pref {
        val x by pref(-4.5, Inch)
        val y by pref(9, Inch)
        ({ UomVector(x, y) })
    }
    private val distanceVerticalConstant by pref(472, Foot)
    private val aspect0 by pref {
        val thor by pref(226)
        val tvert by pref(94)
        ({ thor / tvert.toDouble() })
    }

    private val table = NetworkTableInstance.getDefault().getTable("/limelight")

    private fun l(key: String) = table.getEntry(key).getDouble(0.0)
    private fun targetExists() = l("tv").roundToInt() == 1
    private fun timeStamp(t: Time) = t - l("tl").milli(Second) - 11.milli(Second)

    private fun distanceToTarget(tvert: Double) = distanceVerticalConstant / tvert
    private fun turn(tx: Angle, distance: Length) = atan(distance * tan(tx) / (distance + mounting.y))
    private fun aspect(thor: Double, tvert: Double) = thor / tvert
    private fun skew(aspect: Double) = acos(aspect.Each / aspect0 minMag 1.Each)

    val targetPosition = sensor {
        (if (targetExists()) {
            val tvert = l("tvert")
            val tx = l("tx").Degree
            val thor = l("thor")

            val distance = distanceToTarget(tvert)
            val turn = turn(tx, distance)
            val skew = skew(aspect(thor, tvert))

            Position(
                    distance * sin(turn) + mounting.x,
                    distance * cos(turn) + mounting.y,
                    skew * tx.signum
            )
        } else null) stampWith timeStamp(it)
    }
            .with(graph("Target X Location", Foot)) { it?.x ?: Double.NaN.Foot }
            .with(graph("Target Y Location", Foot)) { it?.y ?: Double.NaN.Foot }
            .with(graph("Target Bearing", Degree)) { it?.bearing ?: Double.NaN.Degree }

    val targetAngle = sensor {
        (if (targetExists()) {
            val tvert = l("tvert")
            val distance = distanceToTarget(tvert)
            val tx = l("tx").Degree
            turn(tx, distance)
        } else null) stampWith timeStamp(it)
    }

    init {
        EventLoop.runOnTick { time ->
            setOf(targetPosition).forEach {
                it.optimizedRead(time, period)
            }
        }
    }
}