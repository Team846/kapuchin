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
    override val name: String = "Limelight"
    override val priority = Priority.Lowest
    override val period: Time = 20.milli(Second)
    override val syncThreshold: Time = 3.milli(Second)

    private val limelightLead by pref(16, Inch)
    private val distanceAreaConstant by pref(4.95534, Foot)
    private val table = NetworkTableInstance.getDefault().getTable("/limelight")

    private fun l(key: String) = table.getEntry(key).getDouble(0.0)
    private fun targetExists() = l("tv").roundToInt() == 1
    private fun timeStamp(t: Time) = t - l("tl").milli(Second)

    val angleToTarget = sensor {
        val dist = distanceToTarget.optimizedRead(it, syncThreshold).y
        (if (targetExists() && dist != null) {
            val ang = l("tx").Degree
            atan(
                    dist * tan(ang) / (dist + limelightLead)
            )
        } else null) stampWith timeStamp(it)
    }
            .with(graph("Angle to Target", Degree)) { it ?: Double.NaN.Degree }

    val distanceToTarget = sensor {
        (if (targetExists()) {
            distanceAreaConstant / Math.sqrt(l("ta"))
        } else null) stampWith timeStamp(it)
    }
            .with(graph("Distance to Target", Foot)) { it ?: -1.Foot }

    init {
        EventLoop.runOnTick { time ->
            setOf(angleToTarget, distanceToTarget).forEach {
                it.optimizedRead(time, period)
            }
        }
    }
}
