package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.pneumaticTicker
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.ShooterHoodState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class ShooterHoodState(val output: Boolean) { Up(true), Down(false) }

class ShooterHoodComponent(hardware: ShooterHoodHardware) : Component<ShooterHoodComponent, ShooterHoodHardware, ShooterHoodState>(hardware, pneumaticTicker) {

    val launchAngles by pref {
        val hoodDown by pref(50, Degree)
        val hoodUp by pref(20, Degree)
        ({ hoodDown to hoodUp })
    }

    override val fallbackController: ShooterHoodComponent.(Time) -> ShooterHoodState = { Down }

    override fun ShooterHoodHardware.output(value: ShooterHoodState) {
        solenoid.set(value.output)
    }
}

class ShooterHoodHardware : SubsystemHardware<ShooterHoodHardware, ShooterHoodComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Shooter Hood"

    private val solenoidPort = 4

    val solenoid by hardw { Solenoid(solenoidPort) }
}