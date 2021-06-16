package com.lynbrookrobotics.twenty.subsystems.shooter

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems.Companion.sharedTickerTiming
import com.lynbrookrobotics.twenty.Subsystems.Companion.shooterTicker
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState.Down
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class ShooterHoodState(val output: Boolean) { Up(true), Down(false) }

class ShooterHoodComponent(hardware: ShooterHoodHardware) :
    Component<ShooterHoodComponent, ShooterHoodHardware, ShooterHoodState>(hardware, shooterTicker) {

    val hoodUpLaunch by pref(20, Degree)
    val hoodDownLaunch by pref(50, Degree)

    override val fallbackController: ShooterHoodComponent.(Time) -> ShooterHoodState = { Down }

    override fun ShooterHoodHardware.output(value: ShooterHoodState) {
        hoodSolenoid.set(value.output)
    }
}

class ShooterHoodHardware : SubsystemHardware<ShooterHoodHardware, ShooterHoodComponent>() {
    override val period by sharedTickerTiming
    override val syncThreshold = 15.milli(Second)
    override val priority = Priority.Low
    override val name = "Shooter Hood"

    private val hoodSolenoidChannel = 4

    val hoodSolenoid by hardw { Solenoid(hoodSolenoidChannel) }
}