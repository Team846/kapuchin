package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.networktables.NetworkTableInstance
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import info.kunalsheth.units.math.tan
import kotlin.math.roundToInt
import com.lynbrookrobotics.kapuchin.subsystems.limelight.LimelightReading
class LimelightHardware(limelight : LimelightReading) : SubsystemHardware<LimelightHardware, LimelightComponent>() {
    override val name = "Limelight"
    override val priority = Priority.Lowest
    override val period = 30.milli(Second)
    override val syncThreshold = 4.milli(Second)

    val table by hardw { NetworkTableInstance.getDefault().getTable("/limelight") }

    private fun l(key: String) = table.getEntry(key).getDouble(0.0)

    val readings = sensor {
        (if(l("tv").roundToInt()==1){
            LimelightReading(l("tx").Degree,l("ty").Degree,
                            l("tx0").Degree,l("ty0").Degree,
                            l("thor"),l("tvert"),
                            l("ta"))
        }else null) stampWith it} 
            .with(graph("tx",Degree)){it?.tx ?: Double.NaN.Degree}
            .with(graph("ty"))
    val targetPosition = sensor {
        (if (targetExists()) {
            val ty = l("ty")
            val tx = l("tx").Degree
            val thor = l("thor")
            val tvert = l("tvert")

            val distance = targetDistance(ty)
            val skew = skew(aspect(thor, tvert))

            Position(
                    targetX(tx),// + mounting.x,
                    distance,// + mounting.y,
                    skew * tx.signum
            )
        } else null) stampWith timeStamp(it)
    }
            .with(graph("Target X Location", Foot)) { it?.x ?: Double.NaN.Foot }
            .with(graph("Target Y Location", Foot)) { it?.y ?: Double.NaN.Foot }
            .with(graph("Target Bearing", Degree)) { it?.bearing ?: Double.NaN.Degree }

    init {
        uiBaselineTicker.runOnTick { time ->
            setOf(targetPosition, targetAngle).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }
}

