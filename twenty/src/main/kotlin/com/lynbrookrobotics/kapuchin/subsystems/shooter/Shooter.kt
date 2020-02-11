package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.DigitalInput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.sqrt


class ShooterComponent(hardware: ShooterHardware) : Component<ShooterComponent, ShooterHardware, OffloadedOutput>(hardware) {


    override val fallbackController: ShooterComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun ShooterHardware.output(value: OffloadedOutput) {
        if (ballLimitSwitch.get()) {
            value.writeTo(masterFlywheelEsc)
        }
    }



}

class ShooterHardware : SubsystemHardware<ShooterHardware, ShooterComponent>() {
    override val period = 50.Millisecond
    override val syncThreshold = 20.Millisecond
    override val priority = High
    override val name = "Shooter"

    val escConfig by escConfigPref()

    private val leftFlywheelId by pref(10)
    private val rightFlywheelId by pref(11)

    private val ballLimitSwitchId by pref(12)

    val ballLimitSwitch by hardw { DigitalInput(ballLimitSwitchId) }

    /**
     * This motor is a NEO 550 controlling the rotation of the shooter's wheels
     */
    val masterFlywheelEsc by hardw { CANSparkMax(leftFlywheelId, kBrushless) }.configure {
        setupMaster(it, escConfig)
    }
    val slaveFlywheelEsc by hardw { CANSparkMax(rightFlywheelId, kBrushless) }.configure {
        generalSetup(it, escConfig)
        it.follow(masterFlywheelEsc)
     }
}