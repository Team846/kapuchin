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


class LimelightHardware : RobotHardware<LimelightHardware>() {
    override val name = "Limelight"
    override val priority = Priority.Lowest

    private val limelightLead by pref(16, Inch)
    private val distanceAreaConstant by pref(4.95534, Foot)
    private val table = NetworkTableInstance.getDefault().getTable("/limelight")

    private fun l(key: String) = table.getEntry(key).getDouble(0.0)
    private fun targetExists() = l("tv").roundToInt() == 1
    private fun timeStamp(t: Time) = t - l("tl").milli(Second) - 11.milli(Second)

    fun limelightAngleToRobotTurnAngle(distance: Length, angle: Angle) =
            distance * tan(angle) / (distance + limelightLead)

    val roughDistanceToTarget = sensor {
        (if (targetExists()) {
            distanceAreaConstant / Math.sqrt(l("ta"))
        } else null) stampWith timeStamp(it)
    }
            .with(graph("Rough Distance to Target", Foot)) { it ?: Double.NaN.Foot }

    val roughAngleToTarget = sensor {
        (if (targetExists()) {
            l("tx").Degree
        } else null) stampWith timeStamp(it)
    }
            .with(graph("Rough Angle to Target", Degree)) { it ?: Double.NaN.Degree }

    val camtranTargetPosition = sensor {
        (if (targetExists()) {
            val camtran = table.getEntry("camtran").getDoubleArray(
                    doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            )

            Position(
                    camtran[0].Inch,
                    -camtran[2].Inch,
                    camtran[4].Degree
            )
        } else null) stampWith timeStamp(it)
    }
            .with(graph("Target X Location", Foot)) { it?.x ?: Double.NaN.Foot }
            .with(graph("Target Y Location", Foot)) { it?.y ?: Double.NaN.Foot }
            .with(graph("Target Bearing", Degree)) { it?.bearing ?: Double.NaN.Degree }

    init {
        EventLoop.runOnTick { time ->
            setOf(roughAngleToTarget, roughDistanceToTarget, camtranTargetPosition).forEach {
                it.optimizedRead(time, 1.Second)
            }
        }
    }
}