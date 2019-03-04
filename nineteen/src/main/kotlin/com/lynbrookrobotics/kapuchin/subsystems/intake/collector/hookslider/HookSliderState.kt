package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode

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

        fun legalRanges(): List<HookSliderState> {

            val (legal, illegal) = Safeties.currentState(hookSlider = HookSliderState().takeIf { it == Undetermined })
                    .partition { it !in Safeties.illegalStates }
            val mappedLegal = legal.mapNotNull { decode(it) }
            val mappedIllegal = illegal.mapNotNull { decode(it) }


            return when {
                mappedLegal.isEmpty() -> mappedIllegal
                else -> mappedLegal
            }
        }
    }
}
