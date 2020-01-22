package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.networktables.NetworkTableInstance
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.roundToInt
import com.lynbrookrobotics.kapuchin.subsystems.limelight.LimelightReading

enum class Pipeline(val number: Int) {
    Zero(0),One(1), Two(2), Three(3), Four(4)
}

class LimelightComponent(hardware: LimelightHardware) : Component<LimelightComponent, LimelightHardware, Pipeline>(hardware, EventLoop) {

    private val mounting by pref {
        val x by pref(0, Inch)
        val y by pref(0, Inch)
        ({ UomVector(x, y) })
    }
    private val zoomTolerance by pref(10)
    private val targetHeightConstant by pref (107, Inch)
    private val mountingAngleConstant by pref(38,Degree)
    private val mountingHeight by pref(24, Inch)
    private val aspect0 by pref {
        val thor by pref(226)
        val tvert by pref(94)
        ({ thor / tvert.toDouble() })
    }
    private val limelight = hardware.readings.readEagerly(0.Second).withoutStamps

    override val fallbackController: LimelightComponent.(Time) -> Pipeline
        get() = TODO()

    val table = NetworkTableInstance.getDefault().getTable("/limelight")
    private fun l(key: String) = table.getEntry(key).getDouble(0.0)

    private fun targetExists() = l("tv").roundToInt() == 1
    private fun timeStamp(t: Time) = t - l("tl").milli(Second) - 11.milli(Second)

    private fun turn(tx: Angle, distance: Length) = atan(distance * tan(tx) / (distance + mounting.y))
    private fun aspect(thor: Double, tvert: Double) = thor / tvert
    private fun skew(aspect: Double) = acos(aspect.Each / aspect0 minMag 1.Each)
    private fun targetDistance(ty: Double) = (targetHeightConstant-mountingHeight)/ tan(/*mountingAngleConstant+*/ty.Degree)
    private fun targetX(tx: Angle) = (tan(tx) * targetDistance(l("ty")))
    private fun targetPosition(tx: Angle, ty: Double, aspect: Double) = Position(targetX(tx),targetDistance(ty),skew(aspect))
    private fun zoomMode(): Pipeline {
        if (limelight.pipeline == 0)
        {
            val horDegPerPix = 56.Degree/getResolution()[0]
            val verDegPerPix = 41.Degree/getResolution()[1]
            val centerPosX = limelight.tx/horDegPerPix
            val centerPosY = limelight.ty/verDegPerPix
            if(limelight.tx>=0) {
                if (centerPosX + limelight.thor/2 < 240-zoomTolerance
                        || centerPosY + limelight.tvert/2 < 180-zoomTolerance) {
                    return Pipeline.valueOf("One")
                } else {
                    return Pipeline.valueOf("Zero")
                }
            }
            else if(limelight.tx<0){
                if (centerPosX + limelight.thor/2 < -(240-zoomTolerance)
                        || centerPosY - limelight.tvert/2 < -(180-zoomTolerance)) {
                    return Pipeline.valueOf("One")
                } else {
                    return Pipeline.valueOf("Zero")
                }
            }
        }
        else if (limelight.pipeline == 1)
        {
            val horDegPerPix = 28.Degree/getResolution()[0]
            val verDegPerPix = (41/2).Degree/getResolution()[1]
            val centerPosX = limelight.tx/horDegPerPix
            val centerPosY = limelight.ty/verDegPerPix
            if(limelight.tx>0) {
                if (centerPosX + limelight.thor/2 < 160-zoomTolerance
                        || centerPosY + limelight.tvert/2 < 120-zoomTolerance) {
                    return Pipeline.valueOf("One")
                } else {
                    return Pipeline.valueOf("Zero")
                }
            }
            else if(limelight.tx<0){
                if (centerPosX + limelight.thor/2 < -(160-zoomTolerance)
                        || centerPosY - limelight.tvert/2 < -(120-zoomTolerance)) {
                    return Pipeline.valueOf("One")
                } else {
                    return Pipeline.valueOf("Zero")
                }
            }
        }
        return Pipeline.valueOf("Zero")
    }
    private fun getResolution(): Array<Number> {
        val horRes: Number = 1
        val verRes: Number = 1
        if(limelight.pipeline  == 0){
            val horRes = 960.Each
            val verRes = 720.Each
            val horRange = 56.Degree
            val verRange = 41.Degree
        }
        else if(limelight.pipeline == 1)
        {
            val horRes = 320.Each
            val verRes = 240.Each
            val horRange = 41.Degree
            val verRange = 41.Degree
        }
        return arrayOf(horRes,verRes)
    }

    override fun LimelightHardware.output(value: Pipeline) {
        table.getEntry("pipeline").setNumber(value.number)
    }
}