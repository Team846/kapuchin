package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.networktables.NetworkTableInstance
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import info.kunalsheth.units.math.tan
import kotlin.math.roundToInt

class LimelightHardware : RobotHardware<LimelightHardware>() {
    override val name = "Limelight"
    override val priority = Priority.Lowest

    private val mounting by pref {
        val x by pref(0, Inch)
        val y by pref(0, Inch)
        ({ UomVector(x, y) })
    }
    private val targetHeightConstant by pref (107, Inch)
    private val mountingAngleConstant by pref(38,Degree)
    private val mountingHeight by pref(24, Inch)
    private val aspect0 by pref {
        val thor by pref(226)
        val tvert by pref(94)
        ({ thor / tvert.toDouble() })
    }

    private val table = NetworkTableInstance.getDefault().getTable("/limelight")

    private fun l(key: String) = table.getEntry(key).getDouble(0.0)
    private fun targetExists() = l("tv").roundToInt() == 1
    private fun timeStamp(t: Time) = t - l("tl").milli(Second) - 11.milli(Second)

    private fun turn(tx: Angle, distance: Length) = atan(distance * tan(tx) / (distance + mounting.y))
    private fun aspect(thor: Double, tvert: Double) = thor / tvert
    private fun skew(aspect: Double) = acos(aspect.Each / aspect0 minMag 1.Each)
    private fun targetDistance(ty: Double) = (targetHeightConstant-mountingHeight)/tan(mountingAngleConstant+ty.Degree)
    private fun targetX(tx: Angle) = (tan(tx) * targetDistance(l("ty")))

    val targetPosition = sensor {
        (if (targetExists()) {
            val ty = l("ty")
            val tx = l("tx").Degree
            val thor = l("thor")
            val tvert = l("tvert")

            val distance = targetDistance(ty)
            val skew = skew(aspect(thor, tvert))

            Position(
                    (targetX(tx).Inch + mounting.x.Inch).Inch,
                    distance + mounting.y,
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
            val ty = l("ty")
            val distance = targetDistance(ty)
            val tx = l("tx").Degree
            turn(tx, distance)
        } else null) stampWith timeStamp(it)
    }

    init {
        uiBaselineTicker.runOnTick { time ->
            setOf(targetPosition, targetAngle).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }
}