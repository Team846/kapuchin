package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.control.data.stampWith
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.networktables.NetworkTableInstance
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.milli
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * System that retrieves data from the Limelight and calculates the angle to target while accounting for latency
 *
 * @author Nikash Walia
 */
class LimelightSystem: SubsystemHardware<LimelightSystem, Nothing>() {
    override val name: String = "Limelight"
    override val priority = Priority.Lowest
    override val period: Time = 20.milli(Second)
    override val syncThreshold: Time = 3.milli(Second)

    private val cameraAngleRelativeToFront: Angle by pref(0, Degree)
    private val distanceToScreenSizeConstant: Dimensionless by pref(4.95534, Each)
    private val table = NetworkTableInstance.getDefault().getTable("/limelight")

    /**
     * Helper method- retrieves entries from Limelight network tables
     *
     * @param key the key for which the value must be retrieved
     */
    private fun getEntry(key: String) = table.getEntry(key).getDouble(0.0)

    /**
     * Sensor that calculates the angle to target, taking into account camera mounting. Nullable- if there is no target
     */
    val angleToTarget = sensor {
        when {
            getEntry("tv").roundToInt() == 0 -> null
            getEntry("ts").absoluteValue < 2 -> cameraAngleRelativeToFront + getEntry("tx").Degree
            else -> cameraAngleRelativeToFront + getEntry("ty").Degree
        } stampWith it - getEntry("tl").milli(Second)
    }

    /**
     * Sensor that calculates the distance to target using a tuned constant. Nullable- if there is no target
     */
    val distanceToTarget = sensor {
        when (getEntry("tv").roundToInt()) {
            0 -> null
            else -> (distanceToScreenSizeConstant / Math.sqrt(getEntry("ta")))
        } stampWith it - getEntry("tl").milli(Second)

    }
}
