package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve.module

import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.TalonFX
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.kapuchin.timing.monitoring.RealtimeChecker.Companion.realtimeChecker
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.Subsystems.Companion.uiBaselineTicker
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselSlot
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMax.IdleMode
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.DigitalOutput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import com.ctre.phoenix.motorcontrol.FeedbackDevice.IntegratedSensor

class ModuleHardware(
    private val escId: Int,
    private val hallEffectChannel: Int,
    private val idx: Int,
    private val wheelEscId: Int

) : SubsystemHardware<ModuleHardware, ModuleComponent>(), GenericWheelHardware {
    override val angle: Sensor<Angle>
        get() = TODO("Not yet implemented")
    override var position: Sensor<Angle>
        get() = TODO("Not yet implemented")
        set(value) {}
    override val conversions = ModuleConversions(this)
    override val priority: Priority = Priority.RealTime
    override val name: String = "Module"
    override val period: Time = 30.milli(Second)
    override val syncThreshold = 5.milli(Second)

    val escConfig by escConfigPref(
        defaultNominalOutput = 1.5.Volt,
        defaultContinuousCurrentLimit = 25.Ampere,
        defaultPeakCurrentLimit = 35.Ampere
    )

    val invert by pref(false)

    val angleEsc by hardw { CANSparkMax(escId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.inverted = invert
        +it.setIdleMode(IdleMode.kCoast)
    }
    val pidController by hardw { angleEsc.pidController }

    val angleEncoder by hardw { angleEsc.encoder }.configure {
        it.position = 0.0
    }

    val anglePosition = sensor(angleEncoder) {
        conversions.angleEncoder.realPosition(position) stampWith it
    }.with(graph("Angle", Degree))

    private val jitterPulsePinNumber by pref(8)
    private val jitterReadPinNumber by pref(9)

    private val wheelEscInversion by pref(false)
    private val wheelSensorInversion by pref(true)

    private val driftTolerance by pref(0.2, DegreePerSecond)

    val jitterPulsePin by hardw { DigitalOutput(jitterPulsePinNumber) }
    val jitterReadPin by hardw { Counter(jitterReadPinNumber) }

    val wheelEsc by hardw { TalonFX(wheelEscId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = wheelEscInversion
        it.setSensorPhase(wheelSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }

    val wheelPosition = sensor {
        conversions.wheelEncoder.realPosition(
            wheelEsc.getSelectedSensorPosition(idx)
        ) stampWith it
    }.with(graph("Wheel Position", Foot))

    private val odometryTicker = ticker(Priority.RealTime, 10.milli(Second), "Odometry")

    private val escNamed = Named("Wheel ESC Odometry", this)
    
    val wheelVelocity = sensor{
        conversions.wheelEncoder.realVelocity(
            wheelEsc.getSelectedSensorVelocity(idx)
        ) stampWith it
    }.with(graph("Wheel Speed", FootPerSecond))

    init {
        uiBaselineTicker.runOnTick { time ->
            setOf(anglePosition, wheelPosition, wheelVelocity).forEach{
                it.optimizedRead(time, 0.5.Second)
            }
        }
        odometryTicker.runOnTick{time ->
            conversions.wheelOdometry(wheelPosition.optimizedRead(time, syncThreshold).y)
        }
    }
}