package com.lynbrookrobotics.kapuchin.subsystems.climber

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
import info.kunalsheth.units.math.*

sealed class ClimberWinchOutput() {
    data class Running(val esc: OffloadedOutput) : ClimberWinchOutput()
    object Stopped : ClimberWinchOutput()
}

class ClimberWinchComponent(hardware: ClimberWinchHardware) :
    Component<ClimberWinchComponent, ClimberWinchHardware, ClimberWinchOutput>(hardware, pneumaticTicker) {

    val extendSpeed by pref(80, Percent)
    val retractSpeed by pref(80, Percent)

    val winchDelaySafety by pref(1, Second)

    override val fallbackController: ClimberWinchComponent.(Time) -> ClimberWinchOutput = { Stopped }

    private val run = true
    private val brake = false

    private var lastBrake = currentTime
    private var lastWinchRun = currentTime
    override fun ClimberWinchHardware.output(value: ClimberWinchOutput) = when (value) {
        is Stopped -> {
            if (currentTime - lastWinchRun >= winchDelaySafety &&
                masterEsc.appliedOutput == 0.0 &&
                slaveEsc.appliedOutput == 0.0
            ) {
                winchSolenoid.set(brake)
                lastBrake = currentTime
            } else log(Warning) {
                "Cannot brake while \n" +
                        "currentTime - lastWinch == ${currentTime - lastWinchRun withDecimals 2}\n" +
                        "masterEsc.appliedOutput == ${masterEsc.appliedOutput withDecimals 2}\n" +
                        "slaveEsc.appliedOutput == ${slaveEsc.appliedOutput withDecimals 2}"
            }

            PercentOutput(escConfig, 0.Percent).writeTo(masterEsc, pidController)
        }
        is Running -> {
            if (currentTime - lastBrake >= winchDelaySafety && winchSolenoid.get() != brake) {
                value.esc.writeTo(masterEsc, pidController)
                lastWinchRun = currentTime
            } else log(Warning) {
                "Cannot run while \n" +
                        "currentTime - lastBrake == ${currentTime - lastBrake withDecimals 2}\n" +
                        "winchSolenoid.get() == brake"
            }

            winchSolenoid.set(run)
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
    private val winchSolenoidChannel = 0

    val winchSolenoid by hardw { Solenoid(winchSolenoidChannel) }

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