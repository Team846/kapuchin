package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.Pipeline.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun LimelightComponent.autoZoom() = startRoutine("auto zoom") {

    val visionTarget by hardware.readings.readEagerly.withoutStamps
    val currentPipeline by hardware.pipeline.readEagerly.withoutStamps

    infix fun <Q : Quan<Q>> ClosedRange<Q>.minContact(that: ClosedRange<Q>): Boolean = this.start == that.start && this.endInclusive < that.endInclusive
    infix fun <Q : Quan<Q>> ClosedRange<Q>.maxContact(that: ClosedRange<Q>): Boolean = this.start > that.start && this.endInclusive == that.endInclusive

    controller {
        visionTarget?.run {
            if (currentPipeline == ZoomOut) {
                val insideBoxResolution = zoomOutResolution / (zoomMultiplier * 1.0)

                val insideBoxBoundsX = `±`(insideBoxResolution.x / 2 - zoomOutSafetyZone)
                val highInsideBoxBoundsY = 0.0.Pixel..(insideBoxResolution.y - zoomOutSafetyZone)
                val midInsideBoxBoundsY = `±`(insideBoxResolution.y / 2 - zoomOutSafetyZone)
                val lowInsideBoxBoundsY = -(insideBoxResolution.y - zoomOutSafetyZone)..0.0.Pixel

                val angleToPixelsX = (zoomOutResolution.x / zoomOutFov.x.Degree)
                val targetBoxBoundsX = (tx * angleToPixelsX / 1.0.Degree) `±` (thor / 2)

                val angleToPixelsY = (zoomOutResolution.y / zoomOutFov.y.Degree)
                val targetBoxBoundsY = (ty * angleToPixelsY / 1.0.Degree) `±` (tvert / 2)


                if (targetBoxBoundsX `⊆` insideBoxBoundsX && targetBoxBoundsY `⊆` midInsideBoxBoundsY) ZoomInPanMid
                else if (targetBoxBoundsX `⊆` insideBoxBoundsX && targetBoxBoundsY `⊆` highInsideBoxBoundsY) ZoomInPanHigh
                else if (targetBoxBoundsX `⊆` insideBoxBoundsX && targetBoxBoundsY `⊆` lowInsideBoxBoundsY) ZoomInPanLow
                else ZoomOut
            } else if (currentPipeline == ZoomInPanMid) {
                val insideBoxBoundsX = `±`(zoomInResolution.x / 2 - zoomInSafetyZone)
                val insideBoxBoundsY = `±`(zoomInResolution.y / 2 - zoomInSafetyZone)

                val angleToPixelsX = zoomInResolution.x / zoomInFov.x.Degree
                val centerInPixX = tx * angleToPixelsX / 1.0.Degree
                val targetBoxBoundsX = centerInPixX `±` (thor / 2)

                val angleToPixelsY = zoomInResolution.y / zoomInFov.y.Degree
                val centerInPixY = ty * angleToPixelsY / 1.0.Degree
                val targetBoxBoundsY = (centerInPixY) `±` (tvert / 2)

                if (targetBoxBoundsX `⊆` insideBoxBoundsX) {
                    if (targetBoxBoundsY.less(insideBoxBoundsY)) {
                        ZoomInPanLow
                    } else if (targetBoxBoundsY.more(insideBoxBoundsY)) {
                        ZoomInPanHigh
                    } else {
                        ZoomInPanMid
                    }
                } else {
                    ZoomOut
                }
            } else if (currentPipeline == ZoomInPanHigh) {
                val insideBoxBoundsX = `±`(zoomInResolution.x / 2 - zoomInSafetyZone)
                val insideBoxBoundsY = `±`(zoomInResolution.y / 2 - zoomInSafetyZone)

                val angleToPixelsX = zoomInResolution.x / zoomInFov.x.Degree
                val centerInPixX = tx * angleToPixelsX / 1.0.Degree
                val targetBoxBoundsX = centerInPixX `±` (thor / 2)

                val angleToPixelsY = zoomInResolution.y / zoomInFov.y.Degree
                val centerInPixY = ty * angleToPixelsY / 1.0.Degree
                val targetBoxBoundsY = (centerInPixY) `±` (tvert / 2)

                if (targetBoxBoundsX `⊆` insideBoxBoundsX) {
                    if (targetBoxBoundsY.less(insideBoxBoundsY)) {
                        ZoomInPanMid
                    } else if (targetBoxBoundsY.more(insideBoxBoundsY)) {
                        ZoomOut
                    } else {
                        ZoomInPanHigh
                    }
                } else {
                    ZoomOut
                }
            } else if (currentPipeline == ZoomInPanLow) {
                val insideBoxBoundsX = `±`(zoomInResolution.x / 2 - zoomInSafetyZone)
                val insideBoxBoundsY = `±`(zoomInResolution.y / 2 - zoomInSafetyZone)

                val angleToPixelsX = zoomInResolution.x / zoomInFov.x.Degree
                val centerInPixX = tx * angleToPixelsX / 1.0.Degree
                val targetBoxBoundsX = centerInPixX `±` (thor / 2)

                val angleToPixelsY = zoomInResolution.y / zoomInFov.y.Degree
                val centerInPixY = ty * angleToPixelsY / 1.0.Degree
                val targetBoxBoundsY = (centerInPixY) `±` (tvert / 2)

                if (targetBoxBoundsX `⊆` insideBoxBoundsX) {
                    if (targetBoxBoundsY.less(insideBoxBoundsY)) {
                        ZoomOut
                    } else if (targetBoxBoundsY.more(insideBoxBoundsY)) {
                        ZoomInPanMid
                    } else {
                        ZoomInPanLow
                    }
                } else {
                    ZoomOut
                }
            } else ZoomOut
        } ?: ZoomOut
    }
}
