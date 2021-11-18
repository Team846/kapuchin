package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainHardware
import info.kunalsheth.units.generated.*

class SwerveComponent(hardware: SwerveHardware) : Component<SwerveComponent, SwerveHardware, FourSided<Pair<OffloadedOutput, OffloadedOutput>>>(hardware) {
    override val fallbackController: SwerveComponent.(Time) -> FourSided<Pair<OffloadedOutput, OffloadedOutput>> = {
        FourSided(Pair(PercentOutput(hardware.brHardware.escConfig, 0.Percent), PercentOutput(hardware.brHardware.escConfig, 0.Percent)),
            Pair(PercentOutput(hardware.blHardware.escConfig, 0.Percent), PercentOutput(hardware.blHardware.escConfig, 0.Percent)),
            Pair(PercentOutput(hardware.trHardware.escConfig, 0.Percent), PercentOutput(hardware.trHardware.escConfig, 0.Percent)),
            Pair(PercentOutput(hardware.trHardware.escConfig, 0.Percent), PercentOutput(hardware.trHardware.escConfig, 0.Percent)),
        )
    }

    override fun SwerveHardware.output(value: FourSided<Pair<OffloadedOutput, OffloadedOutput>>) {
        value.tr.first.writeTo(topRight.hardware.driveEsc)
        value.tr.second.writeTo(topRight.hardware.steerEsc)

        value.tl.first.writeTo(topLeft.hardware.driveEsc)
        value.tl.second.writeTo(topLeft.hardware.steerEsc)

        value.bl.first.writeTo(bottomLeft.hardware.driveEsc)
        value.bl.second.writeTo(bottomLeft.hardware.steerEsc)

        value.br.first.writeTo(bottomRight.hardware.driveEsc)
        value.br.second.writeTo(bottomRight.hardware.steerEsc)
    }

}

class SwerveHardware : SubsystemHardware<SwerveHardware, SwerveComponent>() {
    override val priority = RealTime
    override val name = "Swerve"
    override val period = 20.Millisecond
    override val syncThreshold = 5.Millisecond

    val trHardware = ModuleHardware(0, 32, 33)
    val tlHardware = ModuleHardware(0, 32, 33)
    val brHardware = ModuleHardware(0, 32, 33)
    val blHardware = ModuleHardware(0, 32, 33)

    val trConversions = ModuleConversions(trHardware)
    val tlConversions = ModuleConversions(tlHardware)
    val brConversions = ModuleConversions(brHardware)
    val blConversions = ModuleConversions(blHardware)

    val topRight = ModuleComponent(trHardware)
    val topLeft = ModuleComponent(tlHardware)
    val bottomRight = ModuleComponent(brHardware)
    val bottomLeft = ModuleComponent(blHardware)

}

class SwerveConversions(hardware: SwerveHardware) {



}