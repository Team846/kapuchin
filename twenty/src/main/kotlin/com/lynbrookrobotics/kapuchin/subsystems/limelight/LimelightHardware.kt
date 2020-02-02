package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.networktables.NetworkTableInstance
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.roundToInt

class LimelightHardware : SubsystemHardware<LimelightHardware, LimelightComponent>() {
    override val name = "Limelight"
    override val priority = Priority.Lowest
    override val period = 30.milli(Second)
    override val syncThreshold = 4.milli(Second)

    val table by hardw { NetworkTableInstance.getDefault().getTable("/limelight") }
    val pipelineEntry by hardw { table.getEntry("pipeline") }

    private fun l(key: String) = table.getEntry(key).getDouble(0.0)
    private infix fun <Q> Q.lstamp(withTime: Time) = TimeStamped(
            withTime - l("tl").milli(Second) - 11.milli(Second), this
    )

    val readings = sensor {
        when {
            l("tv").roundToInt() == 1 -> LimelightReading(
                    l("tx").Degree, l("ty").Degree,
                    l("tx0").Pixel, l("ty0").Pixel,
                    l("thor").Pixel, l("tvert").Pixel,
                    l("ta").Pixel// this is actually Pixels Squared
                    //l("panX"), l("panY")
            )
            else -> null
        } lstamp it
    }

    val pipeline = sensor {
        val getpipe = l("getpipe").roundToInt()
        Pipeline.values().firstOrNull() { getpipe == it.number } lstamp it
    }
            .with(graph("pipeline", Each)) { it?.number?.Each ?: -1.Each }

    init {
        uiBaselineTicker.runOnTick { time ->
            setOf(readings, pipeline).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }
}

