package com.lynbrookrobotics.nineteen.subsystems.collector

import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.nineteen.Subsystems
import com.lynbrookrobotics.nineteen.subsystems.collector.HookPosition.Up
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

    val solenoidPort = 2
    val solenoid = Solenoid(solenoidPort)
}