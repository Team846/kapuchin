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
import info.kunalsheth.units.math.sin
import info.kunalsheth.units.math.cos
import info.kunalsheth.units.math.tan
import info.kunalsheth.units.generated.Angle

class LimelightHardware : SubsystemHardware<LimelightHardware, Nothing>() {
    override val name: String = "Limelight"
    override val priority = Priority.Lowest
    override val period: Time = 20.milli(Second)
    override val syncThreshold: Time = 3.milli(Second)

    private val limelightLead by pref(16, Inch)
    private val distanceAreaConstant by pref(4.95534, Foot)
    private val table = NetworkTableInstance.getDefault().getTable("/limelight")

    val verticalDistanceFromCenter by pref(0, Inch)

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
    val skewAngle = sensor {
        val dist = distanceToTarget.optimizedRead(it, syncThreshold).y
        (if (targetExists() && dist != null) {
            val ts = l("ts").Degree
            val toRadian: Double = Math.PI / 180.0
            0.Degree
        } else null) stampWith timeStamp(it)
    }

    //this function is if the robot is angled toward the normal
    val distanceToNormal = sensor {
        val skew = skewAngle.optimizedRead(it, syncThreshold).y!!
        val distanceTo = distanceToTarget.optimizedRead(it, syncThreshold).y
        val angleTo = angleToTarget.optimizedRead(it, syncThreshold).y
        (if (targetExists() && distanceTo != null && angleTo != null) {
            (distanceTo * sin(skew - angleTo) / sin(skew)) + verticalDistanceFromCenter
        } else null) stampWith timeStamp(it)
    }
    val distanceToTarget = sensor {
        (if (targetExists()) {
            distanceAreaConstant / Math.sqrt(l("ta"))
        } else null) stampWith timeStamp(it)
    }
            .with(graph("Distance to Target", Foot)) { it ?: -1.Foot }
    val targetPosition = sensor {

        val distance = distanceToTarget.optimizedRead(100.milli(Second), 100.milli(Second)).y
        val skew = skewAngle.optimizedRead(100.milli(Second), 100.milli(Second)).y
        val robotAngleToTarget = angleToTarget.optimizedRead(100.milli(Second), 100.milli(Second)).y
        (if (targetExists() && distance != null && robotAngleToTarget != null && skew != null) {
            val positionOfTarget: Position = Position(distance * cos(robotAngleToTarget), distance * sin(robotAngleToTarget), skew)
        } else null) stampWith timeStamp(it)
    }

    init {
        EventLoop.runOnTick { time ->
            setOf(angleToTarget, distanceToTarget).forEach {
                it.optimizedRead(time, period)
            }
        }
    }
}
