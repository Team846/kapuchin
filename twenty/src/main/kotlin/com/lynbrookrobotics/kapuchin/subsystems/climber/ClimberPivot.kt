package com.lynbrookrobotics.kapuchin.subsystems.climber

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.pneumaticTicker
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.sharedTickerTiming
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.ClimberPivotState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class ClimberPivotState(val output: Boolean) { Down(false), Up(true) }

class ClimberPivotComponent(hardware: ClimberPivotHardware) : Component<ClimberPivotComponent, ClimberPivotHardware, ClimberPivotState>(hardware, pneumaticTicker) {

    val retractDelay by pref(1, Second)

    override val fallbackController: ClimberPivotComponent.(Time) -> ClimberPivotState = { Down }

    override fun ClimberPivotHardware.output(value: ClimberPivotState) {
        pivotSolenoid.set(value.output)
    }
}

class ClimberPivotHardware : SubsystemHardware<ClimberPivotHardware, ClimberPivotComponent>() {
    override val period by sharedTickerTiming
    override val syncThreshold = 100.milli(Second)
    override val priority = Priority.Low
    override val name = "Climber Pivot"

    private val pivotSolenoidChannel = 1

    val pivotSolenoid by hardw { Solenoid(pivotSolenoidChannel) }
}