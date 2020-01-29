package com.lynbrookrobotics.kapuchin.subsystems.climber

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*

class ClimberWinchHardware : SubsystemHardware<ClimberWinchHardware, ClimberWinchComponent>() {
    override val period: Time = 30.Millisecond
    override val name: String = "Climber Winch"
    override val syncThreshold: Time = 20.Millisecond
    override val priority: Priority = Low

    val escConfig by escConfigPref()

    private val winchEscId by pref(14)
    val winch by hardw { CANSparkMax(winchEscId, kBrushless) }
}

class ClimberWinchComponent(hardware: ClimberWinchHardware) : Component<ClimberWinchComponent, ClimberWinchHardware, OffloadedOutput>(hardware) {
    override val fallbackController: ClimberWinchComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun ClimberWinchHardware.output(value: OffloadedOutput) {
        value.writeTo(winch)
    }
}