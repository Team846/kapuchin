package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.Pipeline.*
import info.kunalsheth.units.generated.*

suspend fun LimelightComponent.autoZoom() = startRoutine("auto zoom") {

    val visionTarget by hardware.readings.readEagerly.withoutStamps
    val currentPipeline by hardware.pipeline.readEagerly.withoutStamps

    controller {
        visionTarget?.run {
            if (currentPipeline == ZoomOut) {
                val insideBoxResolution = zoomOutResolution / zoomMultiplier
                val insideBoxBoundsX = `±`(insideBoxResolution.x / 2 - zoomSafetyZone)
                val insideBoxBoundsY = `±`(insideBoxResolution.y / 2 - zoomSafetyZone)

                val angleToPixelsX = zoomOutResolution.x / zoomOutFov.x
                val targetBoxBoundsX = (tx * angleToPixelsX) `±` (thor / 2)

                val angleToPixelsY = zoomOutResolution.y / zoomOutFov.y
                val targetBoxBoundsY = (ty * angleToPixelsY) `±` (tvert / 2)

                if (targetBoxBoundsX in insideBoxBoundsX && targetBoxBoundsY in insideBoxBoundsY) ZoomIn
                else ZoomOut
            } else if (currentPipeline == ZoomIn) {
                val insideBoxBoundsX = `±`(zoomInResolution.x / 2 - zoomSafetyZone)
                val insideBoxBoundsY = `±`(zoomInResolution.y / 2 - zoomSafetyZone)

                val angleToPixelsX = zoomInResolution.x / zoomInFov.x
                val targetBoxBoundsX = (tx * angleToPixelsX) `±` (thor / 2)

                val angleToPixelsY = zoomInResolution.y / zoomInFov.y
                val targetBoxBoundsY = (ty * angleToPixelsY) `±` (tvert / 2)

                if (targetBoxBoundsX in insideBoxBoundsX && targetBoxBoundsY in insideBoxBoundsY) ZoomIn
                else ZoomOut
            } else currentPipeline
        } ?: ZoomOut
    }
}