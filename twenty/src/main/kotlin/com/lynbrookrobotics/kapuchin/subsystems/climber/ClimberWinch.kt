package com.lynbrookrobotics.kapuchin.subsystems.climber

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class ClimberWinchComponent(hardware: ClimberWinchHardware) : Component<ClimberWinchComponent, ClimberWinchHardware, OffloadedOutput>(hardware) {

    val extendSpeed by pref(80, Percent)
    val retractSpeed by pref(80, Percent)

    override val fallbackController: ClimberWinchComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun ClimberWinchHardware.output(value: OffloadedOutput) {
        value.writeTo(masterEsc, pidController)
    }
}

class ClimberWinchHardware : SubsystemHardware<ClimberWinchHardware, ClimberWinchComponent>() {
    override val period: Time = 30.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val priority: Priority = Priority.Medium
    override val name: String = "Climber Winch"

    private val invertMaster by pref(false)
    private val invertSlave by pref(false)

    val escConfig by escConfigPref()

    private val masterEscId = 10
    private val slaveEscId = 11

    val masterEsc by hardw { CANSparkMax(masterEscId, kBrushless) }.configure {
        generalSetup(it, escConfig)
        it.inverted = invertMaster
    }

    val slaveEsc by hardw { CANSparkMax(slaveEscId, kBrushless) }.configure {
        generalSetup(it, escConfig)
        +it.follow(masterEsc, invertMaster != invertSlave)
    }

    val pidController by hardw { masterEsc.pidController!! }
}