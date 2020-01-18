package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.DigitalInput
import info.kunalsheth.units.generated.*

class ShooterComponent(hardware: ShooterHardware) : Component<ShooterComponent, ShooterHardware, TwoSided<DutyCycle>>(hardware) {
    override val fallbackController: ShooterComponent.(Time) -> TwoSided<DutyCycle>
        get() = { TwoSided(0.Percent, 0.Percent) }

    override fun ShooterHardware.output(value: TwoSided<DutyCycle>) {
        if (ballLimitSwitch.get()) {
            leftFlywheelEsc.set(value.left.Each)
            rightFlywheelEsc.set(value.right.Each)
        }
    }
}

class ShooterHardware : SubsystemHardware<ShooterHardware, ShooterComponent>() {
    override val period: Time
        get() = 50.Millisecond
    override val syncThreshold: Time
        get() = 20.Millisecond
    override val priority: Priority
        get() = Priority.High
    override val name: String
        get() = "Shooter"

    private val leftFlywheelId by pref(10)
    private val rightFlywheelId by pref(11)

    private val ballLimitSwitchId by pref(12)

    val ballLimitSwitch by hardw { DigitalInput(ballLimitSwitchId) }

    /**
     * This motor is a NEO 550 controlling the rotation of the shooter's wheels
     */
    val leftFlywheelEsc by hardw { CANSparkMax(leftFlywheelId, kBrushless) }
    val rightFlywheelEsc by hardw { CANSparkMax(rightFlywheelId, kBrushless) }
}