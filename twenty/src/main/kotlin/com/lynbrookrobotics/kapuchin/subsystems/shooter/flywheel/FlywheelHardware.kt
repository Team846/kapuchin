package com.lynbrookrobotics.kapuchin.subsystems.shooter.flywheel

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class FlywheelHardware : SubsystemHardware<FlywheelHardware, FlywheelComponent>() {
    override val period = sharedTickerTiming()
    override val syncThreshold = sharedTickerTiming()
    override val priority = Priority.High
    override val name = "Shooter Flywheel"

    private val invertMaster by pref(false)
    private val invertSlave by pref(true)

    val escConfig by escConfigPref(
            defaultContinuousCurrentLimit = 30.Ampere,
            defaultPeakCurrentLimit = 60.Ampere,
            defaultVoltageCompSaturation = 11.Volt
    )

    private val masterEscId = 50
    private val slaveEscId = 51

    val masterEsc by hardw { CANSparkMax(masterEscId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.inverted = invertMaster
        it.idleMode = CANSparkMax.IdleMode.kCoast
    }

    val slaveEsc by hardw { CANSparkMax(slaveEscId, kBrushless) }.configure {
        generalSetup(it, escConfig)
        it.follow(masterEsc, invertMaster != invertSlave)
        it.idleMode = CANSparkMax.IdleMode.kCoast
    }

    val conversions = FlywheelConversions(this)

    val pidController by hardw { masterEsc.pidController }

    val encoder by hardw { masterEsc.encoder }
    val speed = sensor(encoder) {
        conversions.encoder.realVelocity(velocity) stampWith it
    }.with(graph("Speed", Rpm))

    init {
        Subsystems.uiBaselineTicker.runOnTick { time ->
            setOf(speed).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }
}