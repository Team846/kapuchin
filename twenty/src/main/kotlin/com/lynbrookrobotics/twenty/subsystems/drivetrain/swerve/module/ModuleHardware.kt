package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve.module

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselSlot
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMax.IdleMode
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class ModuleHardware : SubsystemHardware<ModuleHardware, ModuleComponent>(), GenericWheelHardware {
    override val angle: Sensor<Angle>
        get() = TODO("Not yet implemented")
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

    private val escId = 60
    private val hallEffectChannel = 1
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

    val position = sensor(angleEncoder) {
        conversions.angleEncoder.realPosition(position) stampWith it
    }.with(graph("Angle", Degree))

    private val hallEffect by hardw { DigitalInput(hallEffectChannel) }.configure { dio ->
        dio.requestInterrupts {
            angleEncoder.position = conversions.angleEncoder.native(
                position.optimizedRead(
                    dio.readFallingTimestamp().Second, syncThreshold
                ).y
            )
        }
        dio.setUpSourceEdge(false, true)
        dio.enableInterrupts()
    }

    private val jitterPulsePinNumber by pref(8)
    private val jitterReadPinNumber by pref(9)

    private val wheelEscInversion by pref(false)
    private val wheelSensorInversion by pref(true)

    private val driftTolerance by pref(0.2, DegreePerSecond)

    private val idx = 0
    private val wheelEscId = 30

    val jitterPulsePin by hardw { DigitalOutput(jitterPulsePinNumber) }
    val jitterReadPin by hardw { Counter(jitterReadPinNumber) }

    val wheelEsc by hardw { TalonFX(leftMasterEscId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = wheelEscInversion
        it.setSensorPhase(wheelSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }

    val wheelPosition = sensor {
        conversions.wheelEncoder.encoder.realPosition(
            wheelEsc.getSelectedSensorPosition(idx)
        ) stampWith it
    }.with(graph("Wheel Position", Foot))
}