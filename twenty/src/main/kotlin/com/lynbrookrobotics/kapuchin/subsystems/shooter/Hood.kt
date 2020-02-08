package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class HoodState(val output: Boolean) { Up(true), Down(false) }

class HoodComponent(hardware: HoodHardware) : Component<HoodComponent, HoodHardware, HoodState>(hardware, Subsystems.pneumaticTicker) {

    val launchAngles by pref {
        val hoodDown by pref(50, Degree)
        val hoodUp by pref(20, Degree)
        ({hoodDown to hoodUp})
    }

    override val fallbackController: HoodComponent.(Time) -> HoodState = { HoodState.Down }

    override fun HoodHardware.output(value: HoodState) {
        solenoid.set(value.output)
    }
}

class HoodHardware : SubsystemHardware<HoodHardware, HoodComponent>() {
    override val priority = Priority.Low
    override val period = 100.milli(Second)
    override val syncThreshold = 20.milli(Second)
    override val name = "Shooter Hood"

    private val solenoidPort = 0 // TODO
    val solenoid = Solenoid(solenoidPort)
}