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

    controller {
        visionTarget?.run {
            if (currentPipeline == ZoomOut) {
                val insideBoxResolution = zoomOutResolution / zoomMultiplier

                val insideBoxBoundsX = `±`(insideBoxResolution.x / 2 - zoomOutSafetyZone)
                val highInsideBoxBoundsY = 0.Pixel..(insideBoxResolution.y - zoomOutSafetyZone)
                val midInsideBoxBoundsY = `±`(insideBoxResolution.y / 2 - zoomOutSafetyZone)
                val lowInsideBoxBoundsY = 0.Pixel..-(insideBoxResolution.y - zoomOutSafetyZone)

                val angleToPixelsX = zoomOutResolution.x / zoomOutFov.x
                val targetBoxBoundsX = (tx * angleToPixelsX) `±` (thor / 2)

                val angleToPixelsY = zoomOutResolution.y / zoomOutFov.y
                val targetBoxBoundsY = (ty * angleToPixelsY) `±` (tvert / 2)

                if (targetBoxBoundsX in insideBoxBoundsX && targetBoxBoundsY in midInsideBoxBoundsY) ZoomInPanMid
                else if (targetBoxBoundsX in insideBoxBoundsX && targetBoxBoundsY in highInsideBoxBoundsY) ZoomInPanHigh
                else if (targetBoxBoundsX in insideBoxBoundsX && targetBoxBoundsY in lowInsideBoxBoundsY) ZoomInPanLow
                else ZoomOut
            } else if (currentPipeline == ZoomInPanMid) {
                val insideBoxBoundsX = `±`(zoomInResolution.x / 2 - zoomInSafetyZone)
                val insideBoxBoundsY = `±`(zoomInResolution.y / 2 - zoomInSafetyZone)

                val angleToPixelsX = zoomInResolution.x / zoomInFov.x
                val centerInPixX = tx * angleToPixelsX
                val targetBoxBoundsX = centerInPixX `±` (thor / 2)

                val angleToPixelsY = zoomInResolution.y / zoomInFov.y
                val centerInPixY = ty * angleToPixelsY
                val targetBoxBoundsY = (centerInPixY) `±` (tvert / 2)

                if (targetBoxBoundsX in insideBoxBoundsX && targetBoxBoundsY in insideBoxBoundsY) {

                    ZoomInPanMid
                } else ZoomOut
            } else ZoomOut
        } ?: ZoomOut
    }
}
