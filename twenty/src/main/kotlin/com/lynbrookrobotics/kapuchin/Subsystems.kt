package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.ControlPanelPivotComponent.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class Subsystems(val drivetrain: DrivetrainComponent,

                 val driver: DriverHardware,
                 val operator: OperatorHardware,

                 val collectorRollers: CollectorRollersComponent?,
                 val storageBelt: StorageBeltComponent?,
                 val barAdjustment: BarAdjustmentComponent?,
                 val climberStow: ClimberStowComponent?,
                 val climberWinch: ClimberWinchComponent?,
                 val controlPanelPivot: ControlPanelPivotComponent?,
                 val controlWheel: ControlWheelComponent?,
                 val shooter: ShooterComponent?,
                 val feederRoller: FeederRollerComponent?,
                 val turret: TurretComponent?) : Named by Named("Subsystems") {

    fun teleop() {
        System.gc()
    }

    fun warmup() {
        System.gc()
    }

    companion object : Named by Named("Subsystem") {
        var instance: Subsystems? = null
            private set
        val pneumaticTicker = ticker(Medium, 50.milli(Second), "Pneumatic System Ticker")
        val uiBaselineTicker = ticker(Lowest, 500.milli(Second), "UI Baseline Ticker")


        fun concurrentInit() {
            val drivetrainHardware = DrivetrainHardware()
            val drivetrain = DrivetrainComponent(drivetrainHardware)

            val driverHardware = DriverHardware()
            val operatorHardware = OperatorHardware()

            val collectorRollersHardware = CollectorRollersHardware()
            val collectorRollers = CollectorRollersComponent(collectorRollersHardware)

            val storageBeltHardware = StorageBeltHardware()
            val storageBelt = StorageBeltComponent(storageBeltHardware)

            val barAdjustmentHardware = BarAdjustmentHardware()
            val barAdjustment = BarAdjustmentComponent(barAdjustmentHardware)

            val climberStowHardware = ClimberStowHardware()
            val climberStow = ClimberStowComponent(climberStowHardware)

            val climberWinchHardware = ClimberWinchHardware()
            val climberWinch = ClimberWinchComponent(climberWinchHardware)

            val controlPanelHardware = ControlPanelPivotHardware()
            val controlPanel = ControlPanelPivotComponent(controlPanelHardware)

            val controlWheelHardware = ControlWheelHardware()
            val controlWheel = ControlWheelComponent(controlWheelHardware)
            
            val shooterHardware = ShooterHardware()
            val shooter = ShooterComponent(shooterHardware)

            val feederRollerHardware = FeederRollerHardware()
            val feederRoller = FeederRollerComponent(feederRollerHardware)

            val turretHardware = TurretHardware()
            val turret = TurretComponent(turretHardware)

            instance = Subsystems(
                drivetrain, driverHardware, operatorHardware, collectorRollers, storageBelt, barAdjustment, climberStow, climberWinch, controlPanel, controlWheel, shooter, feederRoller, turret
            )
        }
    }
}