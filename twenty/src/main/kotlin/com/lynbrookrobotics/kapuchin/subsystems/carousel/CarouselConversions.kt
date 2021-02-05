package com.lynbrookrobotics.kapuchin.subsystems.carousel

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import edu.wpi.first.wpilibj.util.Color
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.pow

class CarouselConversions(hardware: CarouselHardware) : Named by Named("Conversions", hardware) {
    var state = 0
    private var firstShot = true

    val encoder by pref {
        val gearbox by pref {
            val motor by pref(1)
            val output by pref(20)
            ({ GearTrain(motor, output) })
        }
        val complianceWheelRadius by pref(1.25, Inch)
        val carouselRadius by pref(10, Inch)

        ({
            AngularOffloadedNativeConversion(
                ::p, ::p, ::p, ::p,
                nativeOutputUnits = 1, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                nativeFeedbackUnits = 1,
                perFeedbackQuantity = gearbox.inputToOutput(1.Turn) * complianceWheelRadius / carouselRadius,
                nativeTimeUnit = 1.Minute, nativeRateUnit = 1.milli(Second)
            )
        })
    }

    val ballIrRange by pref {
        val min by pref(87.5, Percent)
        val max by pref(100, Percent)
        ({ min..max })
    }
    val ballColor by pref {
        val r by pref(29.8, Percent)
        val g by pref(53.2, Percent)
        val b by pref(16.8, Percent)
        ({ Color(r.Each, g.Each, b.Each) })
    }
    val ballColorTolerance by pref(0.1)

    fun similarity(colorA: Color, colorB: Color = ballColor) =
        (colorA.red - colorB.red).pow(2) +
                (colorA.green - colorB.green).pow(2) +
                (colorA.blue - colorB.blue).pow(2)

    fun detectingBall(proximity: Dimensionless, color: Color) =
        proximity in ballIrRange && similarity(color) < ballColorTolerance

    /**
     * @param current - Carousels current angle position
     * @return - null if carousel is full,
     *           otherwise angle to move to after picking ball
     */
    fun loadBallAngle(current: Angle): `∠`? {
        if (state == 5) return null
        if (!firstShot) {
            firstShot = true
            return (state * 72.Degree + current - 36.Degree)%(360.Degree)
        }
        firstShot = true
        state++
        return (72.Degree + current)%(360.Degree)
    }

    /**
     * @param current - Carousels current angle position
     * @return - 0 if carousel is full since there's nothing to do,
     *           otherwise the angle to move to so that the slot at shooter is empty and slot before is full
     */
    fun moveToShootingPos(current: Angle): `∠` {
        if (state == 5) {
            log(Warning) { "Empty carousel" }
            return 0.Degree
        }
        return ((5 - state) * 72.Degree + current)%(360.Degree)
    }

    /**
     * @param current - Carousels current angle position
     * @return - null if carousel is empty but angle to move to shoot a ball if carousel isn't empty
     */
    fun shootBallAngle(current: Angle): `∠`? {
        if (state == 0) {
            log(Warning) { "No ball was there to shoot" }
            return null
        }
        state--
        if (firstShot) {
            firstShot = false
            return (36.Degree + current)%(360.Degree)
        }
        return (72.Degree + current)%(360.Degree)

    }
}