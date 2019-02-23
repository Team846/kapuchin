package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.HookSliderState.Companion.pos
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.HookSliderState.Companion.states
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import kotlin.math.pow

sealed class HookSliderState(val output: Boolean) {
    object In : HookSliderState(false)
    object Out : HookSliderState(true)

    companion object {
        val states = arrayOf(HookSliderState.In, HookSliderState.Out)
        val pos = 5
        operator fun invoke() = Subsystems.hookSlider.hardware.solenoid.get().let {
            when (it) {
                HookSliderState.In.output -> HookSliderState.In
                HookSliderState.Out.output -> HookSliderState.Out
                else -> null
            }
        }
    }
}

fun HookSliderComponent.legalRanges() = Safeties.currentState(hookSlider = null)
        .filter { it !in Safeties.illegalStates }
        .mapNotNull { decode(it) }