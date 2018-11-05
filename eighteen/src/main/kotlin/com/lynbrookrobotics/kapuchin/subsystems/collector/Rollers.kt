package com.lynbrookrobotics.kapuchin.subsystems.collector

import com.lynbrookrobotics.kapuchin.control.electrical.voltageToDutyCycle
import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.ElectricalSystemHardware
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*
import com.lynbrookrobotics.kapuchin.timing.currentTime

class RollersComponent(hardware: RollersHardware, private val electrical: ElectricalSystemHardware) : Component<RollersComponent, RollersHardware, TwoSided<V>>(hardware, EventLoop) {

    val purgeStrength by pref(12, Volt)
    val collectStrength by pref(9, Volt)
    val cubeAdjustCycle by pref(4, Hertz)
    val cubeAdjustStrength by pref(3, Volt)
    val cubeHoldStrength by pref(4, Volt)

    override val fallbackController: RollersComponent.(Time) -> TwoSided<V> = { TwoSided(-cubeHoldStrength) }

    override fun RollersHardware.output(value: TwoSided<V>) {
        val vBat = electrical.batteryVoltage.optimizedRead(currentTime, syncThreshold).value
        leftEsc.set(voltageToDutyCycle(value.left, vBat).siValue)
        rightEsc.set(voltageToDutyCycle(value.right, vBat).siValue)
    }
}

class RollersHardware : SubsystemHardware<RollersHardware, RollersComponent>() {
    override val priority get() = TODO()
    override val period get() = TODO()
    override val syncThreshold = 50.milli(Second)
    override val subsystemName = "Collector Rollers"

    val leftPwmPort by pref(1)
    val leftEsc by hardw { Spark(leftPwmPort) }

    val rightPwmPort by pref(0)
    val rightEsc by hardw { Spark(rightPwmPort) }.configure { it.inverted = true }
}
