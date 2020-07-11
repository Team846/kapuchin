package com.lynbrookrobotics.kapuchin.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.revrobotics.ColorMatch
import edu.wpi.first.wpilibj.util.Color
import info.kunalsheth.units.generated.*

class ControlPanelConversions(hardware: ControlPanelSpinnerHardware) : Named by Named("Color Detection", hardware) {

    private fun Named.pref(defaultR: Double, defaultG: Double, defaultB: Double) = pref {
        val r by pref(defaultR)
        val g by pref(defaultG)
        val b by pref(defaultB)
        ({ Color(r, g, b) })
    }

    val red by pref(0.398, 0.410, 0.192)
    val green by pref(0.207, 0.539, 0.254)
    val blue by pref(0.304, 0.543, 0.153)
    val yellow by pref(0.304, 0.543, 0.153)

    val panelPattern = listOf(red, green, blue, yellow)
    val colorMatcher = ColorMatch().apply {
        panelPattern.forEach { addColorMatch(it) }
    }

    val controlPanelRadius by pref(16, Inch)
    val complianceWheelRadius by pref(1.5, Inch)

    fun encoderPositionDelta(motor: Angle): Angle = motor * complianceWheelRadius / controlPanelRadius

    fun indexColor(color: Color): Int? = colorMatcher
        .matchColor(color)
        ?.color
        ?.let(panelPattern::indexOf)
        .takeIf { it in 0..3 }

    fun colorPositionDelta(lastIndex: Int, currentIndex: Int): Angle {
        require(lastIndex in 0..3)
        require(currentIndex in 0..3)

        val positiveIndex = (lastIndex + 1) % 4
        val negativeIndex = (4 + lastIndex - 1) % 4

        return when (currentIndex) {
            positiveIndex -> 45.Degree
            negativeIndex -> -45.Degree
            else -> {
                log(Error) {
                    "Seem to have missed color transition.\n" +
                            "Transitioned from $lastIndex to $currentIndex"
                }
                0.Degree
            }
        }
    }
}