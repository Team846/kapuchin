package com.lynbrookrobotics.kapuchin.subsystems.hook

import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.hook.HookPosition.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class HookPosition {
    Up, Down
}

class HookComponent(hardware: HookHardware) : Component<HookComponent, HookHardware, HookPosition>(hardware, EventLoop) {
    override val fallbackController: HookComponent.(Time) -> HookPosition = { HookPosition.Up }
    override fun HookHardware.output(value: HookPosition) {
        when (value) {
            Up -> solenoid.set(false)
            Down -> solenoid.set(true)
        }
    }

}

class HookHardware : SubsystemHardware<HookHardware, HookComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 30.milli(Second)
    override val syncThreshold: Time = 5.milli(Second)
    override val name: String = "Hook Slider"

    val solenoidPort by pref(0)
    val solenoid = Solenoid(solenoidPort)

}