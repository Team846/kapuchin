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
import kotlin.math.pow


class LimelightHardware : SubsystemHardware<LimelightHardware, Nothing>() {
    override val name: String = "Limelight"
    override val priority = Priority.Lowest
    override val period: Time = 20.milli(Second)
    override val syncThreshold: Time = 3.milli(Second)

    private val limelightLead by pref(16, Inch)
    private val distanceAreaConstant by pref(4.95534, Foot)
    private val table = NetworkTableInstance.getDefault().getTable("/limelight")
//    private val vshift1 by pref(20.141)
////    private val vshift2 by pref(0.968961)
////    private val quad1 by pref(-49.3353, Degree)
////    private val quad2 by pref(206.895, Degree)
////    private val quadTrans by pref(-147.572, Degree)


    private val distanceFromFront by pref(0, Inch)

    private fun l(key: String) = table.getEntry(key).getDouble(0.0)
    private fun targetExists() = l("tv").roundToInt() == 1
    private fun timeStamp(t: Time) = t - l("tl").milli(Second)
    private fun l2(key: String) = table.getEntry(key).getDoubleArray(doubleArrayOf(0.0))

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
//    val skewAngle = sensor {
//        val ta = l("ta")
//        val dist = distanceToTarget.optimizedRead(it, syncThreshold).y
//        val taMax = vshift1*(vshift2).pow(dist)
//        val ratio = ta/taMax
//        (if (targetExists() && dist != null) {
//            ((quad1*(ratio).pow(2.0)) + (quad2*(ratio)) + quadTrans)
//
//        } else null) stampWith timeStamp(it)
//    }

    //this function is if the robot is angled toward the normal
    val distanceToNormal = sensor {
        val skew = skewAngle.optimizedRead(it, syncThreshold).y
        val distanceTo = distanceToTarget.optimizedRead(it, syncThreshold).y
        val angleTo = angleToTarget.optimizedRead(it, syncThreshold).y
        (if (targetExists() && distanceTo != null && angleTo != null && skew != null) {
            ((distanceTo * sin(skew - angleTo) / sin(skew)) + distanceFromFront)
        } else null) stampWith timeStamp(it)
    }

    // This has to be fixed by accounting for skew angle
    // Solved with the new LL update (2019.5)
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

    val targetPos = sensor {
        val camtran = l2("camtran")
        val x = camtran[0]
        val y = camtran[1]

        (if(targetExists()) {
            doubleArrayOf(x, y)
        } else null) stampWith timeStamp(it)
    }

    val skewAngle = sensor{
        val camtran = l2("camtran")
        val skew = camtran[4]

        (if(targetExists()) {
             skew.Degree
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