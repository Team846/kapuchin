package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.roundToInt

enum class Pipeline(val number: Int) {
    One(1), Two(2), Three(3), Four(4)
}

class LimelightComponent(hardware: LimelightHardware) : Component<LimelightComponent, LimelightHardware, Pipeline>(hardware, EventLoop) {
    override val fallbackController: LimelightComponent.(Time) -> Pipeline
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    private val mounting by pref {
        val x by pref(0, Inch)
        val y by pref(0, Inch)
        ({ UomVector(x, y) })
    }
    private val targetHeightConstant by pref (107, Inch)
    private val mountingAngleConstant by pref(38,Degree)
    private val mountingHeight by pref(24, Inch)
    private val aspect0 by pref {
        val thor by pref(226)
        val tvert by pref(94)
        ({ thor / tvert.toDouble() })
    }
    private fun targetExists() = l("tv").roundToInt() == 1
    private fun timeStamp(t: Time) = t - l("tl").milli(Second) - 11.milli(Second)

    private fun turn(tx: Angle, distance: Length) = atan(distance * tan(tx) / (distance + mounting.y))
    private fun aspect(thor: Double, tvert: Double) = thor / tvert
    private fun skew(aspect: Double) = acos(aspect.Each / aspect0 minMag 1.Each)
    private fun targetDistance(ty: Double) = (targetHeightConstant-mountingHeight)/ tan(/*mountingAngleConstant+*/ty.Degree)
    private fun targetX(tx: Angle) = (tan(tx) * targetDistance(l("ty")))
    private fun targetPosition() = TODO()

    override fun LimelightHardware.output(value: Pipeline) {
        table.getEntry("pipeline").setNumber(value.number)
    }
}