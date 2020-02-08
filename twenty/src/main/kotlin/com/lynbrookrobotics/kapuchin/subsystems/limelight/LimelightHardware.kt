package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.lang.Double.NaN
import kotlin.Double.Companion
import kotlin.math.roundToInt

class LimelightHardware : SubsystemHardware<LimelightHardware, LimelightComponent>() {
    override val name = "Limelight"
    override val priority = Priority.Lowest
    override val period = 30.milli(Second)
    override val syncThreshold = 4.milli(Second)

    val table: NetworkTable by hardw {
        NetworkTableInstance.getDefault().getTable("/limelight")
    }.verify("limelight connected") {
        !it.getEntry("tx").getDouble(NaN).isNaN()
    }
    val pipelineEntry by hardw { table.getEntry("pipeline") }

    private fun l(key: String) = table.getEntry(key).getDouble(0.0)
    private infix fun <Q> Q.lstamp(withTime: Time) = TimeStamped(
            withTime - l("tl").milli(Second) - 11.milli(Second), this //11 is limelight internal latency
    )

    val readings = sensor {
        when {
            l("tv").roundToInt() == 1 -> LimelightReading(
                    l("ty").Degree, l("tx").Degree,
                    l("ty0").Pixel, l("tx0").Pixel,
                    l("tvert").Pixel, l("thor").Pixel,
                    l("ta").Pixel,// this is actually Pixels Squared
                    l("getpipe").roundToInt().let { rawpipe ->
                        Pipeline.values().firstOrNull { it.number == rawpipe }
                    }
            )
            else -> null
        } lstamp it
    }

    init {
        uiBaselineTicker.runOnTick { time ->
            setOf(readings).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }
}
