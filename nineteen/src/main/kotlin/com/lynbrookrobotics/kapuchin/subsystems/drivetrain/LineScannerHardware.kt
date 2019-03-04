package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.Subsystems.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.DigitalOutput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class LineScannerHardware : RobotHardware<LineScannerHardware>() {
    override val priority = Priority.Medium
    override val name = "Line Scanner"

    val exposurePort by pref(2)
    val thresholdPort by pref(3)
    val feedbackPort by pref(4)

    private val lineScanner by hardw {
        LineScanner(
                DigitalOutput(exposurePort),
                DigitalOutput(thresholdPort),
                DigitalInput(feedbackPort)
        )
    }

//    private val exposure by pref(10, Millisecond)
//    private val threshold by pref(25, Percent)
//    private val scanWidth by pref(12, Inch)

    val sideShift by pref(8, Inch) // This is the same as x+l_64
    val range by pref(53.13, Degree) // Field of vision
    val height by pref(6, Inch) // Mounting height
    val m by pref {// Angle between the height and the inner edge of the fov
        ({atan(sideShift / height)- (range / 2)})
    }
    val sideShift2 by pref {// This is the same as x
        ({height * tan(m)})
    }
    val lengthArray = arrayListOf<Length>()

//    val each = lineScanner(exposure, threshold).y!!.Each
//
//    val distFromHeight = sideShift - each.Inch
//    val angle = atan((scanWidth + distFromHeight) / height) - range / 2
//    val input: Angle = Angle(each)
//
//    val linePosition = sensor(lineScanner) { _ -> // This is something old but I was told to not delete it
//        val (x, y) = lineScanner(exposure,
//                threshold)
//        y?.let {
//            (height * sin(input)) / (cos(angle) * cos(angle + input))
//        } stampWith x
//    }
//            .with(graph("Line Position", Inch)) { it ?: 0.Inch }

    fun distance(pix: Int) = (height * tan(m + (pix*range)/127)) - sideShift2 // The formula for calculating distance

    fun genDistArray(): ArrayList<Length> { // Generates an array of all possible lengths
        for (n in 0..127) {
            lengthArray.add(distance(n))
        }
        return lengthArray
    }

    fun getPixDist(pix: Int): Length { // Method that gets the right value from the length array
        return lengthArray[pix]
    }


//    init {
//        uiBaselineTicker.runOnTick { time ->
//            setOf(linePosition).forEach {
//                it.optimizedRead(time, 1.Second)
//            }
//        }
//    }
}