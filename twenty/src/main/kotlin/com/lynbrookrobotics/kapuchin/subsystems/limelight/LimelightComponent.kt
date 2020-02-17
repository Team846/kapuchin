package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*

class LimelightComponent(hardware: LimelightHardware) : Component<LimelightComponent, LimelightHardware, Pipeline?>(hardware, EventLoop) {

    val conversions = LimelightConversions(hardware)

    override val fallbackController: LimelightComponent.(Time) -> Pipeline? = { Pipeline.ZoomOut }

    override fun LimelightHardware.output(value: Pipeline?) {
        pipelineEntry.setNumber(value?.number)
    }
}