package com.lynbrookrobotics.twenty.subsystems.climber

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMax.SoftLimitDirection
import com.revrobotics.CANSparkMax.SoftLimitDirection.kForward
import com.revrobotics.CANSparkMax.SoftLimitDirection.kReverse
import com.revrobotics.CANSparkMaxLowLevel.MotorType
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import info.kunalsheth.units.math.log

enum class ClimberBrakeState(val output: Boolean) { On(false), Off(true) }

sealed class ClimberWinchOutput {
    data class Running(val esc: OffloadedOutput) : ClimberWinchOutput()
    data class RunningNoSafety(val esc: OffloadedOutput) : ClimberWinchOutput()
    object Stopped : ClimberWinchOutput()
}

class ClimberWinchComponent(hardware: ClimberWinchHardware) :
    Component<ClimberWinchComponent, ClimberWinchHardware, ClimberWinchOutput>(hardware, Subsystems.pneumaticTicker) {

    val extendSpeed by pref(50, Percent)
    val retractSpeed by pref(-50, Percent)
    val extendSlowSpeed by pref(10, Percent)
    val retractSlowSpeed by pref(-10, Percent)
    var previousSoftLimit = hardware.masterEsc.getSoftLimit(kForward)

    private val chodeDelaySafety by pref(1, Second)

    override val fallbackController: ClimberWinchComponent.(Time) -> ClimberWinchOutput = { ClimberWinchOutput.Stopped }

    private var lastBrakeTime = currentTime

    override fun ClimberWinchHardware.output(value: ClimberWinchOutput) = when (value) {
        is ClimberWinchOutput.Stopped -> {
            if (masterEsc.appliedOutput == 0.0 && slaveEsc.appliedOutput == 0.0) {
                brakeSolenoid.set(ClimberBrakeState.On.output)
                lastBrakeTime = currentTime
            }

            masterEsc.set(0.0)
        }
        is ClimberWinchOutput.Running -> {
            masterEsc.enableSoftLimit(kReverse, true)
            if(masterEsc.encoder.position < previousSoftLimit) previousSoftLimit = masterEsc.encoder.position
            masterEsc.setSoftLimit(kReverse, previousSoftLimit.toFloat())
            if (currentTime - lastBrakeTime >= chodeDelaySafety && brakeSolenoid.get() != ClimberBrakeState.On.output) {
                value.esc.writeTo(masterEsc, pidController)
            }

            brakeSolenoid.set(ClimberBrakeState.Off.output)
        }
        is ClimberWinchOutput.RunningNoSafety -> {
            masterEsc.enableSoftLimit(kReverse, false)
            if (currentTime - lastBrakeTime >= chodeDelaySafety && brakeSolenoid.get() != ClimberBrakeState.On.output) {
                value.esc.writeTo(masterEsc, pidController)
            }

            brakeSolenoid.set(ClimberBrakeState.Off.output)

        }
    }
}

class ClimberWinchHardware : SubsystemHardware<ClimberWinchHardware, ClimberWinchComponent>() {
    override val period by Subsystems.sharedTickerTiming
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.Medium
    override val name = "Climber Winch"

    private val invert by pref(false)

    val escConfig by escConfigPref(
        defaultContinuousCurrentLimit = 40.Ampere,
        defaultPeakOutput = 3.Volt
    )

    private val masterEscId = 10
    private val slaveEscId = 11
    private val climberSolenoidChannel = 0

    val brakeSolenoid by hardw { Solenoid(climberSolenoidChannel) }

    val masterEsc by hardw { CANSparkMax(masterEscId, MotorType.kBrushless) }.configure {
        generalSetup(it, escConfig)
        it.inverted = invert
        +it.enableSoftLimit(SoftLimitDirection.kReverse, true)
        +it.setSoftLimit(SoftLimitDirection.kReverse, it.encoder.position.toFloat())
    }

    val slaveEsc by hardw { CANSparkMax(slaveEscId, MotorType.kBrushless) }.configure {
        generalSetup(it, escConfig)
        +it.follow(masterEsc)
    }

    val pidController by hardw { masterEsc.pidController!! }
}
