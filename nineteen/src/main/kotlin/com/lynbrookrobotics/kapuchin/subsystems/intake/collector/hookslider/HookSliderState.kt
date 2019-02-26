package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.HookSliderState.Companion.pos
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.HookSliderState.Companion.states
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import kotlin.math.pow

enum class HookSliderState(val output: Boolean) {

    In(false),
    Out(true),
    Undetermined(true);

    companion object {
        val states = arrayOf(HookSliderState.In, HookSliderState.Out)
        val pos = 5
        operator fun invoke() = Subsystems.instance.hookSlider?.hardware?.solenoid?.get().let {
            if (it == null) {
                HookSliderState.Undetermined
            } else {
                when (it) {
                    HookSliderState.In.output -> HookSliderState.In.also { println("HookSliderState: In") }
                    HookSliderState.Out.output -> HookSliderState.Out.also { println("HookSliderState: Out") }
                    else -> HookSliderState.Undetermined
                }
            }
        }

        fun legalRanges() = Safeties.currentState(hookSlider = HookSliderState().takeIf { it == Undetermined })
                    .filter { it !in Safeties.illegalStates }
                    .mapNotNull { decode(it) }
    }
}
