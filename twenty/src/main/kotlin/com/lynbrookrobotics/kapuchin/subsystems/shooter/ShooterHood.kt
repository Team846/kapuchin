package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.ShooterHoodState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class ShooterHoodComponent(hardware: ShooterHoodHardware) : Component<ShooterHoodComponent, ShooterHoodHardware, ShooterHoodState>(hardware, Subsystems.pneumaticTicker) {

    override val fallbackController: ShooterHoodComponent.(Time) -> ShooterHoodState = { Down }

    override fun ShooterHoodHardware.output(value: ShooterHoodState) {
        solenoid.set(value.output)
    }
}

class ShooterHoodHardware : SubsystemHardware<ShooterHoodHardware, ShooterHoodComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "ShooterHood"

    private val solenoidPort = 0
    val solenoid by hardw { Solenoid(solenoidPort) }

}

enum class ShooterHoodState(val output: Boolean) { Up(true), Down(false) }