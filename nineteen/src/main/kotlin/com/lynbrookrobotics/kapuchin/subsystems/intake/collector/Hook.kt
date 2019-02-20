package com.lynbrookrobotics.kapuchin.subsystems.intake.collector

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.HookPosition.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class HookPosition(val output: Boolean) {
    Up(false), Down(true)
}

class HookComponent(hardware: HookHardware) : Component<HookComponent, HookHardware, HookPosition>(hardware, Subsystems.pneumaticTicker) {

    override val fallbackController: HookComponent.(Time) -> HookPosition = { Up }

    override fun HookHardware.output(value: HookPosition) {
        solenoid.set(value.output)
    }
}

class HookHardware : SubsystemHardware<HookHardware, HookComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Hook"

    val solenoidPort by pref(1)
    val solenoid = Solenoid(solenoidPort)
}