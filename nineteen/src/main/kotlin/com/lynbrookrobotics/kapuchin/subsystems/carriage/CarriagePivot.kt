package com.lynbrookrobotics.kapuchin.subsystems.carriage

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.carriage.CarriagePivotPosition.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class CarriagePivotPosition {
    Up, Down
}

class CarriagePivotComponent(hardware: CarriagePivotHardware) : Component<CarriagePivotComponent, CarriagePivotHardware, CarriagePivotPosition>(hardware) {

    override val fallbackController: CarriagePivotComponent.(Time) -> CarriagePivotPosition = { CarriagePivotPosition.Up }

    override fun CarriagePivotHardware.output(value: CarriagePivotPosition) {
        when(value) {
            Up -> hardware.leftSolenoid.set(true)
            Down -> hardware.rightSolenoid.set(false)
        }
    }

}

class CarriagePivotHardware : SubsystemHardware<CarriagePivotHardware, CarriagePivotComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 50.milli(Second)
    override val name: String = "Rollers"

    val leftSolenoidPort by pref(0)
    val leftSolenoid by hardw { Solenoid(leftSolenoidPort) }

    val rightSolenoidPort by pref(1)
    val rightSolenoid by hardw { Solenoid(rightSolenoidPort) }
}