package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.networktables.NetworkTableInstance
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.acos
import kotlin.math.roundToInt

class LimelightHardware : SubsystemHardware<LimelightHardware, Nothing>() {
    override val name = "Limelight"
    override val priority = Priority.Lowest
    override val period = 20.milli(Second)
    override val syncThreshold = 3.milli(Second)

    private val mounting by pref {
        val x by pref(-6, Inch)
        val y by pref(16, Inch)
        ({ UomVector(x, y) })
    }
    private val distanceVerticalConstant by pref(472, Foot)
    private val distanceHorizontalConstant by pref {
        val thor by pref(226)
        val forDistance by pref(5, Foot)
        ({ forDistance * thor })
    }

    private val table = NetworkTableInstance.getDefault().getTable("/limelight")

    private fun l(key: String) = table.getEntry(key).getDouble(0.0)
    private fun targetExists() = l("tv").roundToInt() == 1
    private fun timeStamp(t: Time) = t - l("tl").milli(Second) - 11.milli(Second)

    private fun distanceToTarget(tvert: Double) = distanceVerticalConstant / tvert
    private fun thorMax(distance: Length) = distanceHorizontalConstant / distance
    private fun skew(thor: Dimensionless, thorMax: Dimensionless) = acos(
            if(thor > thorMax) 1.Each else thor / thorMax
    )
    fun turn(tx: Angle, distance: Length) = atan(distance * tan(tx) / (distance + mounting.y))

//    val roughTargetLocation = sensor {
//        (if (targetExists()) {
//            val tx = l("tx").Degree
//            val
//
//
//            UomVector(
//                    distance * sin(direction),
//                    distance * cos(direction)
//            ) + mounting
//        } else null) stampWith timeStamp(it)
//    }
//            .with(graph("Rough Target X Location", Foot)) { it?.x ?: Double.NaN.Foot }
//            .with(graph("Rough Target Y Location", Foot)) { it?.y ?: Double.NaN.Foot }

    val targetPosition = sensor {
        (if (targetExists()) {
            val tvert = l("tvert")
            val distance = distanceToTarget(tvert)
            val tx = l("tx").Degree
            val turn = turn(tx, distance)
            val thor = l("thor")
            val thorMax = thorMax(distance)
            val targetBearing = skew(thor.Each, thorMax)

            Position(
                    distance * sin(turn) + mounting.x,
                    distance * cos(turn) + mounting.y,
                    targetBearing * tx.signum
            )
        } else null) stampWith timeStamp(it)
    }
            .with(graph("Target X Location", Foot)) { it?.x ?: Double.NaN.Foot }
            .with(graph("Target Y Location", Foot)) { it?.y ?: Double.NaN.Foot }
            .with(graph("Target Bearing", Degree)) { it?.bearing ?: Double.NaN.Degree }

    init {
        EventLoop.runOnTick { time ->
            setOf(targetPosition).forEach {
                it.optimizedRead(time, period)
            }
        }
    }
}