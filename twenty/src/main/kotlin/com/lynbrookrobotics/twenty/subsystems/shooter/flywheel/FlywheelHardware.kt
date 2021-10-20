package com.lynbrookrobotics.twenty.subsystems.shooter.flywheel

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMax.IdleMode
import com.revrobotics.CANSparkMaxLowLevel.MotorType
import edu.wpi.first.networktables.NetworkTableInstance
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class FlywheelHardware : SubsystemHardware<FlywheelHardware, FlywheelComponent>() {
    override val period by Subsystems.sharedTickerTiming
    override val syncThreshold = 5.milli(Second)
    override val priority = Priority.High
    override val name = "Shooter Flywheel"

    private val invertMaster by pref(false)
    private val invertSlave by pref(true)

    val escConfig by escConfigPref(
        defaultContinuousCurrentLimit = 30.Ampere,
        defaultPeakCurrentLimit = 60.Ampere,
        defaultVoltageCompSaturation = 11.Volt
    )

    val conversions = FlywheelConversions(this)

    private val masterEscId = 50
    private val slaveEscId = 51

    val masterEsc by hardw { CANSparkMax(masterEscId, MotorType.kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.inverted = invertMaster
        +it.setIdleMode(IdleMode.kCoast)
    }

    @Suppress("unused")
    val slaveEsc by hardw { CANSparkMax(slaveEscId, MotorType.kBrushless) }.configure {
        generalSetup(it, escConfig)
        +it.follow(masterEsc, invertMaster != invertSlave)
        +it.setIdleMode(IdleMode.kCoast)
    }

    val pidController by hardw { masterEsc.pidController!! }
    val encoder by hardw { masterEsc.encoder!! }

    val speed = sensor(encoder) {
        conversions.encoder.realVelocity(velocity) stampWith it
    }.with(graph("Speed", Rpm))

    init {
        Subsystems.fastUiTicker.runOnTick { time -> // TODO revert
            speed.optimizedRead(time, Subsystems.fastUiTicker.period)
            NetworkTableInstance.getDefault().flush()
        }
    }
}