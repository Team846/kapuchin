package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider

import com.lynbrookrobotics.kapuchin.*

val hookSliderStates = arrayOf(HookSliderPosition.In, HookSliderPosition.Out)
fun HookSliderState() = Subsystems.hookSlider.hardware.solenoid.get().let {
    when (it) {
        HookSliderPosition.In.output -> HookSliderPosition.In
        HookSliderPosition.Out.output -> HookSliderPosition.Out
        else -> null
    }
}

private fun HookSliderComponent.decode(state: RobotState): HookSliderPosition? {
    val hookSliderCode = state.code and HookSliderPosition.hookSliderQueryCode
    return when (hookSliderCode) {
        0b00_00_000_0_1 -> HookSliderPosition.In
        0b00_00_000_0_0 -> HookSliderPosition.Out
        else -> null
    }
}

fun HookSliderComponent.legalRanges() = Safeties.currentState(hookSlider = null)
        .filter { it !in Safeties.illegalStates }
        .mapNotNull { decode(it) }