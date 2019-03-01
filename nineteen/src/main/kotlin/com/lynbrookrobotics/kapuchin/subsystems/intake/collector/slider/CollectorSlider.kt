package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANDigitalInput.LimitSwitchPolarity.kNormallyClosed
import com.revrobotics.CANDigitalInput.LimitSwitch.kForward
import com.revrobotics.CANSparkMaxLowLevel.MotorType
import edu.wpi.first.wpilibj.DigitalInput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CollectorSliderComponent(hardware: CollectorSliderHardware) : Component<CollectorSliderComponent, CollectorSliderHardware, DutyCycle>(hardware) {

    val kP by pref(3, Volt, 1, Inch)

    override val fallbackController: CollectorSliderComponent.(Time) -> DutyCycle = { 0.Percent }

    override fun CollectorSliderHardware.output(value: DutyCycle) {

        esc.set(value.Percent)

//        val current = position.optimizedRead(currentTime, 0.Second).y
//
//        val range = unionizeAndFindClosestRange(CollectorSliderState.legalRanges(), current, (Int.MIN_VALUE + 1).Inch)
//
//        if (range.start - range.endInclusive != 0.Inch) {
//            when {
//                value.isPositive && range.endInclusive > current ||
//                        value.isNegative && range.start < current
//                -> esc.set(value.Percent)
//                else -> esc.set(0.0)
//            }
//        } else if (Safeties.log) {
//            //log(Warning) { "No legal states found" }
//        }
    }
}

class CollectorSliderHardware : SubsystemHardware<CollectorSliderHardware, CollectorSliderComponent>() {
    override val priority: Priority = Priority.Medium
    override val period: Time = 30.milli(Second)
    override val syncThreshold: Time = 5.milli(Second)
    override val name: String = "Collector Slider"

    val operatingVoltage by pref(11, Volt)
    val currentLimit by pref(20, Ampere)
    val startupFrictionCompensation by pref(0.5, Volt)

    val invert by pref(false)

    val escCanId by pref(20)

    val esc by hardw { CANSparkMax(escCanId, MotorType.kBrushless) }.configure {
        it.setSmartCurrentLimit(currentLimit.Ampere.toInt())
        it.getReverseLimitSwitch(kNormallyClosed).enableLimitSwitch(true)
        it.inverted = invert
    }
    val encoder by hardw { esc.encoder }

    val conversion by pref {
        val encoderRotations by pref(10, Turn)
        val perSliderDistance by pref(1, Inch)

        val resolution by pref(42)
        ({
            val enc = EncoderConversion(resolution, 1.Turn)
            fun(ticks: Double) = enc.angle(ticks - zeroOffset) / encoderRotations * perSliderDistance
        })
    }

    val position = sensor(encoder) { conversion(position - zeroOffset) stampWith it }
            .with(graph("Position", Inch))
    val velocity = sensor(encoder) { velocity.Rpm stampWith it }
            .with(graph("Velocity", Rpm))
    val atZero = sensor(esc) { getReverseLimitSwitch(kNormallyClosed).get() stampWith it }
            .with(graph("At Zero", Each)) { (if (it) 1 else 0).Each }

    private var zeroOffset = 0.0
    fun zero() {
        zeroOffset = encoder.position
    }

    init {
        uiBaselineTicker.runOnTick { t ->
            setOf(position, velocity, atZero).forEach {
                it.optimizedRead(t, .5.Second)
            }
        }
    }
}