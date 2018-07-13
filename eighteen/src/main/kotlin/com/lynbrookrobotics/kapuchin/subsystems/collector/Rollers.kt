package com.lynbrookrobotics.kapuchin.subsystems.collector

import com.lynbrookrobotics.kapuchin.control.electrical.MotorCurrentLimiter
import com.lynbrookrobotics.kapuchin.control.electrical.voltageToDutyCycle
import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.ElectricalSystemHardware
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*

class RollersComponent(hardware: RollersHardware, electrical: ElectricalSystemHardware) : Component<RollersComponent, RollersHardware, TwoSided<Volt>>(hardware) {

    val purgeStrength by pref(12, `To Volt`)
    val collectStrength by pref(9, `To Volt`)
    val cubeAdjustCycle by pref(4, `To Hertz`)
    val cubeAdjustStrength by pref(3, `To Volt`)
    val cubeHoldStrength by pref(2, `To Volt`)

    override val fallbackController: RollersComponent.(Time) -> TwoSided<Volt> = { TwoSided(-cubeHoldStrength) }

    private val vBat by electrical.batteryVoltage.readEagerly.withoutStamps
    override fun RollersHardware.output(value: TwoSided<Volt>) {
        leftEsc.set(voltageToDutyCycle(target = value.left, vBat = vBat).siValue)
        rightEsc.set(voltageToDutyCycle(target = value.right, vBat = vBat).siValue)
    }
}

class RollersHardware : SubsystemHardware<RollersHardware, RollersComponent>() {
    override val priority = Priority.Medium
    override val period = 50.milli(::Second)
    override val syncThreshold = 5.milli(::Second)
    override val subsystemName = "Collector Rollers"

    val leftPwmPort by pref(1)
    val leftEsc by hardw { Spark(leftPwmPort) }

    val rightPwmPort by pref(0)
    val rightEsc by hardw { Spark(rightPwmPort) }.configure { it.inverted = true }
}
