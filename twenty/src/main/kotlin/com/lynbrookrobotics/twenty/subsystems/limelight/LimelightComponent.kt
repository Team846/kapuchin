package com.lynbrookrobotics.twenty.subsystems.limelight

import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.twenty.Subsystems.Companion.uiBaselineTicker
import info.kunalsheth.units.generated.*

class LimelightComponent(hardware: LimelightHardware) :
    Component<LimelightComponent, LimelightHardware, Pipeline?>(hardware, uiBaselineTicker) {

    val allowInnerGoal by pref(false)

    override val fallbackController: LimelightComponent.(Time) -> Pipeline? = { Pipeline.ZoomOut }

    override fun LimelightHardware.output(value: Pipeline?) {
        pipelineEntry.setNumber(value?.number)
    }
}