package com.lynbrookrobotics.kapuchin.subsystems.climber

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushed
import info.kunalsheth.units.generated.*

class ClimberWinchComponent (hardware: ClimberWinchHardware) : Component<ClimberWinchComponent, ClimberWinchHardware, DutyCycle>(hardware) {
    override val fallbackController: ClimberWinchComponent.(Time) -> DutyCycle ={0.Percent}

    override fun ClimberWinchHardware.output(value: DutyCycle) {
        climberwinchEsc.set(value.Each)




    }



}
class ClimberWinchHardware : SubsystemHardware<ClimberWinchHardware, ClimberWinchComponent>() {
    override val period: Time = 30.Millisecond
    override val syncThreshold: Time = 30.Millisecond
    override val priority: Priority = Priority.Low
    override val name: String = "Climber Winch"

    val climberwinchEscId by pref(10)
    val climberwinchEscInversion by pref(false)

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,
            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    val climberwinchEsc by hardw { CANSparkMax(climberwinchEscId, kBrushed) }.configure {

    }
}