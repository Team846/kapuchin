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
                val highInsideBoxBoundsY = 0.Pixel..(insideBoxResolution.y - zoomOutSafetyZone)
                val midInsideBoxBoundsY = `±`(insideBoxResolution.y / 2 - zoomOutSafetyZone)
                val lowInsideBoxBoundsY = 0.Pixel..-(insideBoxResolution.y - zoomOutSafetyZone)

                val angleToPixelsX = (zoomOutResolution.x / zoomOutFov.x.Degree).Pixel
                val targetBoxBoundsX = (tx * angleToPixelsX) `±` (thor / 2).Pixel

                val angleToPixelsY = (zoomOutResolution.y / zoomOutFov.y.Degree).Pixel
                val targetBoxBoundsY = (ty * angleToPixelsY) `±` (tvert / 2).Pixel

                if (targetBoxBoundsX `⊆` insideBoxBoundsX && targetBoxBoundsY `⊆` midInsideBoxBoundsY) ZoomInPanMid
                else if (targetBoxBoundsX `⊆` insideBoxBoundsX && targetBoxBoundsY `⊆` highInsideBoxBoundsY) ZoomInPanHigh
                else if (targetBoxBoundsX `⊆` insideBoxBoundsX && targetBoxBoundsY `⊆` lowInsideBoxBoundsY) ZoomInPanLow
                else ZoomOut
            } else if (currentPipeline == ZoomInPanMid) {
                val insideBoxBoundsX = `±`(zoomInResolution.x / 2 - zoomInSafetyZone)
                val insideBoxBoundsY = `±`(zoomInResolution.y / 2 - zoomInSafetyZone)

                val angleToPixelsX = (zoomInResolution.x / zoomInFov.x.Degree).Pixel
                val centerInPixX = tx * angleToPixelsX
                val targetBoxBoundsX = centerInPixX `±` (thor / 2)

                val angleToPixelsY = zoomInResolution.y / zoomInFov.y.Degree
                val centerInPixY = ty * angleToPixelsY
                val targetBoxBoundsY = (centerInPixY) `±` (tvert / 2)

                if (insideBoxBoundsX `⊆` targetBoxBoundsX && insideBoxBoundsY `⊆` targetBoxBoundsY) {
                    ZoomInPanMid
                } else if (insideBoxBoundsX `⊆` targetBoxBoundsX && targetBoxBoundsY.minContact(insideBoxBoundsY)) {
                    ZoomInPanLow
                } else if (insideBoxBoundsX `⊆` targetBoxBoundsX && targetBoxBoundsY.maxContact(insideBoxBoundsY)) {
                    ZoomInPanHigh
                } else {
                    ZoomOut
                }
            } else if (currentPipeline == ZoomInPanHigh) {
                val insideBoxBoundsX = `±`(zoomInResolution.x / 2 - zoomInSafetyZone)
                val insideBoxBoundsY = `±`(zoomInResolution.y / 2 - zoomInSafetyZone)

                val angleToPixelsX = zoomInResolution.x / zoomInFov.x.Degree
                val centerInPixX = tx * angleToPixelsX
                val targetBoxBoundsX = centerInPixX `±` (thor / 2)

                val angleToPixelsY = zoomInResolution.y / zoomInFov.y.Degree
                val centerInPixY = ty * angleToPixelsY
                val targetBoxBoundsY = (centerInPixY) `±` (tvert / 2)

                if (insideBoxBoundsX `⊆` targetBoxBoundsX && insideBoxBoundsY `⊆` targetBoxBoundsY) {
                    ZoomInPanHigh
                } else if (insideBoxBoundsX `⊆` targetBoxBoundsX && targetBoxBoundsY.minContact(insideBoxBoundsY)) {
                    ZoomInPanMid
                } else {
                    ZoomOut
                }
            } else if (currentPipeline == ZoomInPanLow) {
                val insideBoxBoundsX = `±`(zoomInResolution.x / 2 - zoomInSafetyZone)
                val insideBoxBoundsY = `±`(zoomInResolution.y / 2 - zoomInSafetyZone)

                val angleToPixelsX = zoomInResolution.x / zoomInFov.x.Degree
                val centerInPixX = tx * angleToPixelsX
                val targetBoxBoundsX = centerInPixX `±` (thor / 2)

                val angleToPixelsY = zoomInResolution.y / zoomInFov.y.Degree
                val centerInPixY = ty * angleToPixelsY
                val targetBoxBoundsY = (centerInPixY) `±` (tvert / 2)

                if (insideBoxBoundsX `⊆` targetBoxBoundsX && insideBoxBoundsY `⊆` targetBoxBoundsY) {
                    ZoomInPanLow
                } else if (insideBoxBoundsX `⊆` targetBoxBoundsX && targetBoxBoundsY.maxContact(insideBoxBoundsY)) {
                    ZoomInPanMid
                } else {
                    ZoomOut
                }
            } else ZoomOut
        } ?: ZoomOut
    }
}
