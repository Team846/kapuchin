package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.shooterTicker
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.sharedTickerTiming
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.ShooterHoodState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*

enum class ShooterHoodState(val output: Boolean, val launchAngle: (ShooterHoodComponent) -> Angle) {
    Up(true, ShooterHoodComponent::hoodUpLaunch),
    Down(false, ShooterHoodComponent::hoodDownLaunch)
}

class ShooterHoodComponent(hardware: ShooterHoodHardware) : Component<ShooterHoodComponent, ShooterHoodHardware, ShooterHoodState>(hardware, shooterTicker) {
    val hoodUpLaunch by pref(20, Degree)
    val hoodDownLaunch by pref(50, Degree)

    override val fallbackController: ShooterHoodComponent.(Time) -> ShooterHoodState = { Down }
    override fun ShooterHoodHardware.output(value: ShooterHoodState) {
        hoodSolenoid.set(value.output)
    }
}

class ShooterHoodHardware : SubsystemHardware<ShooterHoodHardware, ShooterHoodComponent>() {
    override val period = sharedTickerTiming()
    override val syncThreshold = sharedTickerTiming()
    override val priority = Priority.Low
    override val name = "Shooter Hood"

    private val hoodSolenoidChannel = 4
    val hoodSolenoid by hardw { Solenoid(hoodSolenoidChannel) }
}