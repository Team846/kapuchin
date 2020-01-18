package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.subsystems.climber.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.math.*

class Subsystems(val drivetrain: DrivetrainComponent,
                 val driver : DriverHardware,
                 val operator: OperatorHardware,
                 val rumble: RumbleComponent,
                 val leds: LedComponent,
                 val collectorRollers : CollectorRollersComponent?,
                 val storageBelt : StorageBeltComponent?,
                 val barAdjustment : BarAdjustmentComponent?,
                 val climberStow : ClimberStowComponent?,
                 val climberWinch : ClimberWinchComponent?,
                 val controlPanelPivot : ControlPanelPivotComponent?,
                 val controlWheel : ControlWheelComponent?) : Named by Named("Subsystems"){

    suspend fun teleop() {
        System.gc()
    }

    suspend fun warmup() {
        System.gc()
    }

    companion object : Named by Named("Subsystem") {
        private val isCorrupted by pref(true)

        var instance : Subsystems? = null
        val pneumaticTicker = ticker(Medium, 50.milli(Second), "Pneumatic System Ticker")
        fun concurrentInit(){

        }
    }




}