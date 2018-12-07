package com.lynbrookrobotics.kapuchin.subsystems.collector

import com.lynbrookrobotics.kapuchin.control.data.TwoSided
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.milli

class RollersComponent(hardware: RollersHardware) : Component<RollersComponent, RollersHardware, TwoSided<DutyCycle>>(hardware, EventLoop) {

    val purgeStrength by pref(100, Percent)
    val collectStrength by pref(75, Percent)
    val cubeAdjustCycle by pref(4, Hertz)
    val cubeAdjustStrength by pref(25, Percent)
    val cubeHoldStrength by pref(33, Percent)

    override val fallbackController: RollersComponent.(Time) -> TwoSided<DutyCycle> = { TwoSided(-cubeHoldStrength) }

    override fun RollersHardware.output(value: TwoSided<DutyCycle>) {
        leftEsc.set(value.left.Each)
        rightEsc.set(value.right.Each)
    }
}

class RollersHardware : SubsystemHardware<RollersHardware, RollersComponent>() {
    override val priority get() = TODO()
    override val period get() = TODO()
    override val syncThreshold = 50.milli(Second)
    override val name = "Collector Rollers"

    val leftPwmPort by pref(1)
    val leftEsc by hardw { Spark(leftPwmPort) }

    val rightPwmPort by pref(0)
    val rightEsc by hardw { Spark(rightPwmPort) }.configure { it.inverted = true }
}
