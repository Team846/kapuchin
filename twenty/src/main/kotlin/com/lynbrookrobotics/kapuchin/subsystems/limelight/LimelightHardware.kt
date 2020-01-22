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

class LimelightHardware : SubsystemHardware<LimelightHardware, LimelightComponent>() {
    override val name = "Limelight"
    override val priority = Priority.Lowest
    override val period = 30.milli(Second)
    override val syncThreshold = 4.milli(Second)

    val table by hardw { NetworkTableInstance.getDefault().getTable("/limelight") }

    private fun l(key: String) = table.getEntry(key).getDouble(0.0)

    public val readings = sensor {
        when {
            l("tv").roundToInt() == 1 -> LimelightReading(
                    l("tx").Degree, l("ty").Degree,
                    l("tx0"), l("ty0"),
                    l("thor"), l("tvert"),
                    l("ta"), l("getpipe"))
            else -> null
        } stampWith it
    }
            .with(graph("tx", Degree)) { it?.tx ?: Double.NaN.Degree }
            .with(graph("ty", Degree)) { it?.ty ?: Double.NaN.Degree }
            .with(graph("tx0", Each)) { it?.tx0?.Each ?: Double.NaN.Each }
            .with(graph("ty0", Each)) { it?.ty0?.Each ?: Double.NaN.Each }
            .with(graph("thor", Each)) { it?.thor?.Each ?: Double.NaN.Each }
            .with(graph("tvert", Each)) { it?.tvert?.Each ?: Double.NaN.Each }
            .with(graph("ta", Each)) { it?.ta?.Each ?: Double.NaN.Each }
            .with(graph("getpipe",Each)) { it?.pipeline?.Each ?: Double.NaN.Each}


    init {
        uiBaselineTicker.runOnTick { time ->
            setOf(readings).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }
}

