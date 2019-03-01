package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.HookSliderState.Companion.states
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import kotlin.math.pow

enum class HookSliderState(val output: Boolean) {

    In(false),
    Out(true),
    Undetermined(true);

    companion object {
        val states = arrayOf(HookSliderState.In, HookSliderState.Out)
        operator fun invoke() = HookSliderState.In
//                Subsystems.instance?.let {
//            it.hookSlider?.hardware?.solenoid?.get().let {
//                if (it == null) {
//                    HookSliderState.Undetermined
//                } else {
//                    when (it) {
//                        HookSliderState.In.output -> HookSliderState.In
//                        HookSliderState.Out.output -> HookSliderState.Out
//                        else -> HookSliderState.Undetermined
//                    }
//                }
//            }
//        }

            fun legalRanges() = Safeties.currentState(hookSlider = HookSliderState().takeIf { it == Undetermined })
                    .filter { it !in Safeties.illegalStates }
                    .mapNotNull { decode(it) }
        }
    }
