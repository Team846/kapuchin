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
    val cubeAdjustCycle by pref(3, `To Hertz`)
    val cubeAdjustStrength by pref(8, `To Volt`)
    val cubeHoldStrength by pref(4, `To Volt`)

    override val fallbackController: RollersComponent.(Time) -> TwoSided<Volt> = { TwoSided(-cubeHoldStrength) }

    private val vBat by electrical.batteryVoltage.readEagerly.withoutStamps
    private val inputCurrent by electrical.rollersInputCurrent.readEagerly.withoutStamps
    private val currentLimiter by pref {
        val currentLimit by pref(30, `To Ampere`)
        val freeSpeed by pref(19000, `To Rpm`)
        val stallCurrent by pref(85, `To Ampere`)
        ({
            MotorCurrentLimiter(12.Volt, freeSpeed, stallCurrent, currentLimit)
        })
    }

    override fun RollersHardware.output(value: TwoSided<Volt>) {
        val leftDc = leftEsc.get()
        val rightDc = rightEsc.get()

        leftEsc.set(voltageToDutyCycle(
                target = currentLimiter(
                        applying = vBat * leftDc,
                        drawing = inputCurrent.left / leftDc,
                        target = value.left
                ), vBat = vBat
        ).siValue)

        rightEsc.set(voltageToDutyCycle(
                target = currentLimiter(
                        applying = vBat * rightDc,
                        drawing = inputCurrent.left / rightDc,
                        target = value.left
                ), vBat = vBat
        ).siValue)
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
