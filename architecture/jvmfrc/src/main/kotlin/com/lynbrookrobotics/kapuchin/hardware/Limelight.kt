package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.control.data.stampWith
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.networktables.NetworkTableInstance
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.milli

class LimelightSystem: SubsystemHardware<LimelightSystem, Nothing>() {
    override val name: String = "Limelight"
    override val priority = Priority.Lowest
    override val period: Time = 20.milli(Second)
    override val syncThreshold: Time = 3.milli(Second)

    private val cameraIsVertical by pref(true)
    private val cameraAngleRelativeToFront: Angle by pref(0, Degree)

    private val table = NetworkTableInstance.getDefault().getTable("/limelight")
    private fun <Input> s(f: () -> Input) = sensor { f() stampWith it - Millisecond(table.getEntry("tl").getDouble(0.0)) }

    private val angleToTarget = s {
        if (table.getEntry("tv").getDouble(0.0) != 1.0) {
            null
        } else if (cameraIsVertical) {
            cameraAngleRelativeToFront + Degree(table.getEntry("tx").getDouble(0.0))
        } else {
            cameraAngleRelativeToFront + Degree(table.getEntry("ty").getDouble(0.0))
        }
    }
}
