package com.lynbrookrobotics.twenty.subsystems.climber

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems.Companion.pneumaticTicker
import com.lynbrookrobotics.twenty.Subsystems.Companion.sharedTickerTiming
import com.lynbrookrobotics.twenty.subsystems.climber.ClimberWinchOutput.Running
import com.lynbrookrobotics.twenty.subsystems.climber.ClimberWinchOutput.Stopped
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

sealed class ClimberWinchOutput() {
    data class Running(val esc: OffloadedOutput) : ClimberWinchOutput()
    object Stopped : ClimberWinchOutput()
}

class ClimberWinchComponent(hardware: ClimberWinchHardware) :
    Component<ClimberWinchComponent, ClimberWinchHardware, ClimberWinchOutput>(hardware, pneumaticTicker) {

    val extendSpeed by pref(20, Percent)
    val retractSpeed by pref(20, Percent)

    val chodeDelaySafety by pref(1, Second)

    override val fallbackController: ClimberWinchComponent.(Time) -> ClimberWinchOutput = { Stopped }

    private val down = true
    private val up = false

    private var lastUp = currentTime
    private var lastWinchRun = currentTime
    override fun ClimberWinchHardware.output(value: ClimberWinchOutput) = when (value) {
        is Stopped -> {
            if (currentTime - lastWinchRun >= chodeDelaySafety &&
                masterEsc.appliedOutput == 0.0 &&
                slaveEsc.appliedOutput == 0.0
            ) {
                climberSolenoid.set(up)
                lastUp = currentTime
            } else log(Warning) {
                "Cannot brake while \n" +
                        "currentTime - lastWinch == ${currentTime - lastWinchRun withDecimals 2}\n" +
                        "masterEsc.appliedOutput == ${masterEsc.appliedOutput withDecimals 2}\n" +
                        "slaveEsc.appliedOutput == ${slaveEsc.appliedOutput withDecimals 2}"
            }

            PercentOutput(escConfig, 0.Percent).writeTo(masterEsc, pidController)
        }
        is Running -> {
            if (currentTime - lastUp >= chodeDelaySafety && climberSolenoid.get() != up) {
                value.esc.writeTo(masterEsc, pidController)
                lastWinchRun = currentTime
            } else log(Warning) {
                "Cannot run while \n" +
                        "currentTime - lastUp == ${currentTime - lastUp withDecimals 2}\n" +
                        "climberSolenoid.get() == up"
            }

            climberSolenoid.set(down)
        }
    }
}

class ClimberWinchHardware : SubsystemHardware<ClimberWinchHardware, ClimberWinchComponent>() {
    override val period by sharedTickerTiming
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.Medium
    override val name = "Climber Winch"

    private val invert by pref(false)
    val escConfig by escConfigPref(
        defaultContinuousCurrentLimit = 40.Ampere,
        defaultPeakOutput = 3.Volt // TODO: remove extra safe default
    )

    private val masterEscId = 10
    private val slaveEscId = 11
    private val climberSolenoidChannel = 0

    val climberSolenoid by hardw { Solenoid(climberSolenoidChannel) }

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