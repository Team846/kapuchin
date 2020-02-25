package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.Pipeline.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun LimelightComponent.set(target: Pipeline) = startRoutine("Set") {
    controller { target }
}

suspend fun LimelightComponent.autoZoom() = startRoutine("Auto Zoom") {

    val visionTarget by hardware.readings.readEagerly.withoutStamps

    infix fun <Q : Quan<Q>> ClosedRange<Q>.more(that: ClosedRange<Q>): Boolean = this.start > that.start && this.endInclusive > that.endInclusive
    infix fun <Q : Quan<Q>> ClosedRange<Q>.less(that: ClosedRange<Q>): Boolean = this.start < that.start && this.endInclusive < that.endInclusive

    controller {
        with(hardware.conversions) {
            visionTarget?.let { t ->
                val insideBoxBoundsX = `±`(zoomInResolution.x / 2 - zoomInSafetyZone)
                val insideBoxBoundsY = `±`(zoomInResolution.y / 2 - zoomInSafetyZone)

                val angleToPixelsX = zoomInResolution.x / zoomInFov.x
                val centerInPixX = t.tx * angleToPixelsX
                val targetBoxBoundsX = centerInPixX `±` (t.thor / 2)

                val angleToPixelsY = zoomInResolution.y / zoomInFov.y
                val centerInPixY = t.ty * angleToPixelsY
                val targetBoxBoundsY = (centerInPixY) `±` (t.tvert / 2)

                when (t.pipeline) {
                    ZoomOut -> {
                        val insideBoxResolution = zoomOutResolution / zoomMultiplier.toDouble()

                        val insideBoxBoundsX = `±`(insideBoxResolution.x / 2 - zoomOutSafetyZone)
                        val highInsideBoxBoundsY = 0.0.Each..(insideBoxResolution.y - zoomOutSafetyZone)
                        val midInsideBoxBoundsY = `±`(insideBoxResolution.y / 2 - zoomOutSafetyZone)
                        val lowInsideBoxBoundsY = -(insideBoxResolution.y - zoomOutSafetyZone)..0.0.Each

                        val angleToPixelsX = (zoomOutResolution.x / zoomOutFov.x)
                        val targetBoxBoundsX = (t.tx * angleToPixelsX) `±` (t.thor / 2)

                        val angleToPixelsY = (zoomOutResolution.y / zoomOutFov.y)
                        val targetBoxBoundsY = (t.ty * angleToPixelsY) `±` (t.tvert / 2)

                        if (targetBoxBoundsX `⊆` insideBoxBoundsX) {
                            when {
                                targetBoxBoundsY `⊆` lowInsideBoxBoundsY -> ZoomInPanLow
                                targetBoxBoundsY `⊆` midInsideBoxBoundsY -> ZoomInPanMid
                                targetBoxBoundsY `⊆` highInsideBoxBoundsY -> ZoomInPanHigh
                                else -> ZoomOut
                            }
                        } else ZoomOut
                    }
                    ZoomInPanMid -> {
                        if (targetBoxBoundsX `⊆` insideBoxBoundsX) {
                            when {
                                targetBoxBoundsY.less(insideBoxBoundsY) -> ZoomInPanLow
                                targetBoxBoundsY.more(insideBoxBoundsY) -> ZoomInPanHigh
                                else -> ZoomInPanMid
                            }
                        } else ZoomOut
                    }
                    ZoomInPanLow -> {
                        if (targetBoxBoundsX `⊆` insideBoxBoundsX) {
                            when {
                                targetBoxBoundsY.less(insideBoxBoundsY) -> ZoomOut
                                targetBoxBoundsY.more(insideBoxBoundsY) -> ZoomInPanMid
                                else -> ZoomInPanLow
                            }
                        } else ZoomOut
                    }
                    ZoomInPanHigh -> {
                        if (targetBoxBoundsX `⊆` insideBoxBoundsX) {
                            when {
                                targetBoxBoundsY.less(insideBoxBoundsY) -> ZoomInPanMid
                                targetBoxBoundsY.more(insideBoxBoundsY) -> ZoomOut
                                else -> ZoomInPanHigh
                            }
                        } else ZoomOut
                    }
                    else -> ZoomOut
                }

            } ?: ZoomOut
        }
    }
}