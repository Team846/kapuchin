package com.lynbrookrobotics.kapuchin.subsystems.climber

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.pneumaticTicker
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.sharedTickerTiming
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.ClimberWinchOutput.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*

sealed class ClimberWinchOutput() {
    data class Running(val esc: OffloadedOutput) : ClimberWinchOutput()
    object Stopped : ClimberWinchOutput()
}

class ClimberWinchComponent(hardware: ClimberWinchHardware) : Component<ClimberWinchComponent, ClimberWinchHardware, ClimberWinchOutput>(hardware, pneumaticTicker) {

    val extendSpeed by pref(80, Percent)
    val retractSpeed by pref(80, Percent)

    override val fallbackController: ClimberWinchComponent.(Time) -> ClimberWinchOutput = { Stopped }

    private val flaccid = true
    private val erect = false
    override fun ClimberWinchHardware.output(value: ClimberWinchOutput) = when (value) {
        is Stopped -> {
            if (masterEsc.appliedOutput == 0.0 && slaveEsc.appliedOutput == 0.0)
                chodeSolenoid.set(erect)
            else log(Warning) {
                "Cannot brake while \n" +
                        "masterEsc.appliedOutput == ${masterEsc.appliedOutput withDecimals 2}\n" +
                        "slaveEsc.appliedOutput == ${slaveEsc.appliedOutput withDecimals 2}"
            }

            PercentOutput(escConfig, 0.Percent).writeTo(masterEsc, pidController)
        }
        is Running -> {
            if (chodeSolenoid.get() == erect) log(Warning) {
                "Cannot run while chodeSolenoid.get() == erect"
            } else
                value.esc.writeTo(masterEsc, pidController)

            chodeSolenoid.set(flaccid)
        }
    }
}

class ClimberWinchHardware : SubsystemHardware<ClimberWinchHardware, ClimberWinchComponent>() {
    override val period = sharedTickerTiming()
    override val syncThreshold = sharedTickerTiming()
    override val priority = Priority.Medium
    override val name = "Climber Winch"

    private val invert by pref(false)

    val escConfig by escConfigPref(
            defaultContinuousCurrentLimit = 40.Ampere
    )

    private val masterEscId = 10
    private val slaveEscId = 11
    private val chodeSolenoidChannel = 0

    val chodeSolenoid by hardw { Solenoid(chodeSolenoidChannel) }

    val masterEsc by hardw { CANSparkMax(masterEscId, kBrushless) }.configure {
        generalSetup(it, escConfig)
        it.inverted = invert
    }

    val slaveEsc by hardw { CANSparkMax(slaveEscId, kBrushless) }.configure {
        generalSetup(it, escConfig)
        +it.follow(masterEsc)
    }

    val pidController by hardw { masterEsc.pidController }
}