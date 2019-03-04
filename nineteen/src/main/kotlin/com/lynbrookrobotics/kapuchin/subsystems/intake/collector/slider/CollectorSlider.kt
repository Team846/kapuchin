package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANDigitalInput.LimitSwitchPolarity.kNormallyClosed
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CollectorSliderComponent(hardware: CollectorSliderHardware) : Component<CollectorSliderComponent, CollectorSliderHardware, DutyCycle>(hardware) {

    val kP by pref(12, Volt, 1, Inch)

    val min by pref(-3.5, Inch)
    val max by pref(4.75, Inch)
    val maxOutput by pref(100, Percent)

    override val fallbackController: CollectorSliderComponent.(Time) -> DutyCycle = { 0.Percent }

    val nativeGrapher = graph("Output", Percent)
    override fun CollectorSliderHardware.output(value: DutyCycle) {

        val currentAtZero = atZero.optimizedRead(currentTime, 0.Second).y
        val currentPosition = position.optimizedRead(currentTime, 0.Second).y

        val zeroedOutput =
                if (!isZeroed)
                    if (!currentAtZero) -20.Percent
                    else {
                        zero()
                        0.Percent
                    }
                else if (currentPosition > max && value.isPositive) 0.Percent
                else if (currentPosition < min && value.isNegative) 0.Percent
                else value
        val cappedOutput = zeroedOutput minMag (maxOutput * zeroedOutput.siValue)

        esc.set(cappedOutput.Each)
        nativeGrapher(currentTime, cappedOutput)

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
    override val priority: Priority = Priority.RealTime
    override val period: Time = 30.milli(Second)
    override val syncThreshold: Time = 5.milli(Second)
    override val name: String = "Collector Slider"

    val currentLimit by pref(20, Ampere)
    val startupFrictionCompensation by pref(0.5, Volt)

    val invert by pref(true)
    val invertSensor by pref(true)

    val escCanId = 20

    val esc by hardw { CANSparkMax(escCanId, MotorType.kBrushless) }.configure {
        +it.setSmartCurrentLimit(currentLimit.Ampere.toInt())
        +it.setIdleMode(CANSparkMax.IdleMode.kBrake)
        it.inverted = invert
    }
    val encoder by hardw { esc.encoder }
    val limitSwitch by hardw { esc.getForwardLimitSwitch(kNormallyClosed) }.configure {
        +it.enableLimitSwitch(true)
    }

    val conversion by pref {
        val rotations by pref(12, Turn)
        val perSliderDistance by pref(1, Inch)
        ({
            fun(x: Angle) = x / rotations * perSliderDistance
        })
    }

    val nativeGrapher = graph("Native", Turn)
    val position = sensor(encoder) { t ->
        position.Turn
                .let {
                    if (invertSensor) -it else it
                }.also { nativeGrapher(t, it) }
                .let {
                    conversion(it)
                } - zeroOffset stampWith t
    }
            .with(graph("Position", Inch))

    val velocity = sensor(encoder) { t ->
        velocity.Rpm.let {
            if (invertSensor) -it else it
        } stampWith t
    }
            .with(graph("Velocity", Rpm))

    val atZero = sensor(limitSwitch) { get() stampWith it }
            .with(graph("At Zero", Each)) { (if (it) 1 else 0).Each }

    var isZeroed = false
        private set
    private var zeroOffset = 0.Inch
    private val center by pref(4.25, Inch)
    fun zero() {
        zeroOffset = position.optimizedRead(currentTime, 0.Second).y + center
        isZeroed = true
    }

    init {
        uiBaselineTicker.runOnTick { t ->
            setOf(position, velocity, atZero).forEach {
                it.optimizedRead(t, .5.Second)
            }
        }
    }
}