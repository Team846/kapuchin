package com.lynbrookrobotics.twenty.subsystems.limelight

import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.twenty.Subsystems
import info.kunalsheth.units.generated.*

class LimelightComponent(hardware: LimelightHardware) :
    Component<LimelightComponent, LimelightHardware, Pipeline?>(hardware, Subsystems.uiBaselineTicker) {

    override val fallbackController: LimelightComponent.(Time) -> Pipeline? = { Pipeline.ZoomOut }

    override fun LimelightHardware.output(value: Pipeline?) {
        pipelineEntry.setNumber(value?.number)
    }
}