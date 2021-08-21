package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.twenty.subsystems.limelight.LimelightComponent
import com.lynbrookrobotics.twenty.subsystems.limelight.Pipeline
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun LimelightComponent.set(target: Pipeline) = startRoutine("Set") {
    controller { target }
}

suspend fun LimelightComponent.autoZoom() = startRoutine("Auto Zoom") {

    val visionTarget by hardware.readings.readEagerly.withoutStamps

    infix fun <Q : Quan<Q>> ClosedRange<Q>.more(that: ClosedRange<Q>): Boolean =
        this.start > that.start && this.endInclusive > that.endInclusive

    infix fun <Q : Quan<Q>> ClosedRange<Q>.less(that: ClosedRange<Q>): Boolean =
        this.start < that.start && this.endInclusive < that.endInclusive

    controller {
        with(hardware.conversions) {
            val target = visionTarget?.copy()
            if (target != null) {
                var insideBoxBoundsX = `±`(zoomInResolution.x / 2 - zoomInSafetyZone)
                val insideBoxBoundsY = `±`(zoomInResolution.y / 2 - zoomInSafetyZone)

                var angleToPixelsX = zoomInResolution.x / zoomInFov.x
                val centerInPixX = target.tx * angleToPixelsX
                var targetBoxBoundsX = centerInPixX `±` (target.thor / 2)

                var angleToPixelsY = zoomInResolution.y / zoomInFov.y
                val centerInPixY = target.ty * angleToPixelsY
                var targetBoxBoundsY = (centerInPixY) `±` (target.tvert / 2)

                when (target.pipeline) {
                    Pipeline.ZoomOut -> {
                        val insideBoxResolution = zoomOutResolution / zoomMultiplier.toDouble()

                        insideBoxBoundsX = `±`(insideBoxResolution.x / 2 - zoomOutSafetyZone)
                        val lowInsideBoxBoundsY = -(insideBoxResolution.y - zoomOutSafetyZone)..0.0.Each
                        val midInsideBoxBoundsY = `±`(insideBoxResolution.y / 2 - zoomOutSafetyZone)
                        val highInsideBoxBoundsY = 0.0.Each..(insideBoxResolution.y - zoomOutSafetyZone)

                        angleToPixelsX = (zoomOutResolution.x / zoomOutFov.x)
                        targetBoxBoundsX = (target.tx * angleToPixelsX) `±` (target.thor / 2)

                        angleToPixelsY = (zoomOutResolution.y / zoomOutFov.y)
                        targetBoxBoundsY = (target.ty * angleToPixelsY) `±` (target.tvert / 2)

                        if (targetBoxBoundsX `⊆` insideBoxBoundsX) {
                            when {
                                targetBoxBoundsY `⊆` lowInsideBoxBoundsY -> Pipeline.ZoomInPanLow
                                targetBoxBoundsY `⊆` midInsideBoxBoundsY -> Pipeline.ZoomInPanMid
                                targetBoxBoundsY `⊆` highInsideBoxBoundsY -> Pipeline.ZoomInPanHigh
                                else -> Pipeline.ZoomOut
                            }
                        } else Pipeline.ZoomOut
                    }
                    Pipeline.ZoomInPanMid -> {
                        if (targetBoxBoundsX `⊆` insideBoxBoundsX) {
                            when {
                                targetBoxBoundsY.less(insideBoxBoundsY) -> Pipeline.ZoomInPanLow
                                targetBoxBoundsY.more(insideBoxBoundsY) -> Pipeline.ZoomInPanHigh
                                else -> Pipeline.ZoomInPanMid
                            }
                        } else Pipeline.ZoomOut
                    }
                    Pipeline.ZoomInPanLow -> {
                        if (targetBoxBoundsX `⊆` insideBoxBoundsX) {
                            when {
                                targetBoxBoundsY.less(insideBoxBoundsY) -> Pipeline.ZoomOut
                                targetBoxBoundsY.more(insideBoxBoundsY) -> Pipeline.ZoomInPanMid
                                else -> Pipeline.ZoomInPanLow
                            }
                        } else Pipeline.ZoomOut
                    }
                    Pipeline.ZoomInPanHigh -> {
                        if (targetBoxBoundsX `⊆` insideBoxBoundsX) {
                            when {
                                targetBoxBoundsY.less(insideBoxBoundsY) -> Pipeline.ZoomInPanMid
                                targetBoxBoundsY.more(insideBoxBoundsY) -> Pipeline.ZoomOut
                                else -> Pipeline.ZoomInPanHigh
                            }
                        } else Pipeline.ZoomOut
                    }
                    else -> Pipeline.ZoomOut
                }

            } else {
                Pipeline.ZoomOut
            }
        }
    }
}