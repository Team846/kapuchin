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
    private val distanceAreaConstant by pref(4.95534, Foot)
    private val table = NetworkTableInstance.getDefault().getTable("/limelight")

    private fun l(key: String) = table.getEntry(key).getDouble(0.0)
    private fun targetExists() = l("tv").roundToInt() == 1
    private fun timeStamp(t: Time) = t - l("tl").milli(Second) - 11.milli(Second)

//    val roughDistanceToTarget = sensor {
//        (if (targetExists()) {
//            distanceAreaConstant / Math.sqrt(l("ta"))
//        } else null) stampWith timeStamp(it)
//    }
//            .with(graph("Rough Distance to Target", Foot)) { it ?: Double.NaN.Foot }
//
//    val directionOfTarget = sensor {
//        (if (targetExists()) {
//            l("tx").Degree
//        } else null) stampWith timeStamp(it)
//    }
//            .with(graph("Direction to Target", Degree)) { it ?: Double.NaN.Degree }
//
//    fun targetDirectionToRobotTurn(direction: Angle, distance: Length) = atan(
//            distance * tan(direction) / (distance + mounting.y)
//    )

    val precisePositioningRange by pref(5, Foot)

    val roughTargetLocation = sensor {
        (if (targetExists()) {
            val distance = distanceAreaConstant / Math.sqrt(l("ta"))
            val direction = l("tx").Degree

            UomVector(
                    distance * sin(direction),
                    distance * cos(direction)
            ) + mounting
        } else null) stampWith timeStamp(it)
    }
            .with(graph("Rough Target X Location", Foot)) { it?.x ?: Double.NaN.Foot }
            .with(graph("Rough Target Y Location", Foot)) { it?.y ?: Double.NaN.Foot }

    val targetPosition = sensor {
        (if (targetExists()) {
            val camtran = table.getEntry("camtran").getDoubleArray(
                    doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            )

            val distance = -camtran[2].Inch
            val skew = camtran[4].Degree
            val direction = l("tx").Degree

            Position(
                    distance * sin(direction) + mounting.x,
                    distance * cos(direction) + mounting.y,
                    skew
            )
        } else null) stampWith (it)
    }
            .with(graph("Target X Location", Foot)) { it?.x ?: Double.NaN.Foot }
            .with(graph("Target Y Location", Foot)) { it?.y ?: Double.NaN.Foot }
            .with(graph("Target Bearing", Degree)) { it?.bearing ?: Double.NaN.Degree }

    init {
        EventLoop.runOnTick { time ->
            setOf(roughTargetLocation, targetPosition).forEach {
                it.optimizedRead(time, period)
            }
        }
    }
}