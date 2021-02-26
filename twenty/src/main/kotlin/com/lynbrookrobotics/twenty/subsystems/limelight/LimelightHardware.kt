package com.lynbrookrobotics.twenty.subsystems.limelight

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems.Companion.sharedTickerTiming
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.lang.Double.NaN

class LimelightHardware : SubsystemHardware<LimelightHardware, LimelightComponent>() {
    override val period by sharedTickerTiming
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.High
    override val name = "Limelight"

    val invertTx by pref(false)
    val invertTy by pref(true)

    val table: NetworkTable by hardw {
        NetworkTableInstance.getDefault().getTable("/limelight")
    }.verify("Limelight is connected") {
        !it.getEntry("tx").getDouble(NaN).isNaN()
    }
    val pipelineEntry by hardw { table.getEntry("pipeline") }

    private fun l(key: String) = table.getEntry(key).getDouble(0.0)
    private infix fun <Q> Q.lstamp(withTime: Time) = TimeStamped(
        withTime - l("tl").milli(Second) - 11.milli(Second), this
    )

    /**
     * Because the Limelight is mounted sideways,
     * "ty" is passed into the "tx" parameter and "tx" is
     * passed into the "ty" parameter
     *
     * when you want to use the target's horizontal offset, call readings.tx
     * this will return the value that is read as "ty" on the limelight dashboard
     *
     * Same goes for vertical offset - call readings.ty
     */
    val readings = sensor {
        when {
            l("tv").toInt() == 1 -> LimelightReading(
                tx = l("ty").Degree * if (invertTy) -1 else 1,
                ty = l("tx").Degree * if (invertTx) -1 else 1,
                tx0 = l("ty0").Each,
                ty0 = l("tx0").Each,
                l("tvert").Each, l("thor").Each,
                l("ta").Each,// this is actually Pixels Squared
                l("getpipe").toInt().let { rawpipe ->
                    Pipeline.values().firstOrNull { it.number == rawpipe }
                }
            )
            else -> null
        } lstamp it
    }

    val pipeline = sensor {
        l("getpipe").toInt().let { rawpipe ->
            Pipeline.values().firstOrNull { it.number == rawpipe }
        } lstamp it
    }

    val conversions = LimelightConversions(this)
}