package com.lynbrookrobotics.kapuchin.subsystems.hook

import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.hook.HookSliderPosition.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class HookSliderPosition {
    In, Out
}

class HookSliderComponent(hardware: HookSliderHardware) : Component<HookSliderComponent, HookSliderHardware, HookSliderPosition>(hardware, EventLoop) {
    override val fallbackController: HookSliderComponent.(Time) -> HookSliderPosition = { HookSliderPosition.In }
    override fun HookSliderHardware.output(value: HookSliderPosition) {
        when (value) {
            In -> solenoid.set(false)
            Out -> solenoid.set(true)
        }
    }

}

class HookSliderHardware : SubsystemHardware<HookSliderHardware, HookSliderComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 30.milli(Second)
    override val syncThreshold: Time = 5.milli(Second)
    override val name: String = "Hook Slider"

    val solenoidPort by pref(0)
    val solenoid = Solenoid(solenoidPort)

}