package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

@Suppress("unused")
class Subsystems(val drivetrain: DrivetrainComponent,

                 val electrical: ElectricalSystemHardware,
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
                 val turret: TurretComponent?,
                 val limelight: LimelightHardware?
) : Named by Named("Subsystems") {

    fun teleop() {
        System.gc()
    }

    fun warmup() {
        System.gc()
    }

    companion object : Named by Named("Subsystem") {
        private val initCollectorRollers by pref(true)
        private val initStorageBelt by pref(true)
        private val initControlPanelPivot by pref(true)
        private val initControlWheel by pref(false)
        private val initBarAdjustment by pref(false)
        private val initClimberStow by pref(false)
        private val initClimberWinch by pref(true)
        private val initFeederRoller by pref(false)
        private val initTurret by pref(true)
        private val initShooter by pref(true)
        private val initLimelight by pref(true)

        var instance: Subsystems? = null
            private set
        val pneumaticTicker = ticker(Medium, 50.milli(Second), "Pneumatic System Ticker")
        val uiBaselineTicker = ticker(Lowest, 500.milli(Second), "UI Baseline Ticker")

        fun sequentialInit() {
            val drivetrainHardware = DrivetrainHardware()
            val drivetrain = DrivetrainComponent(drivetrainHardware)

            val electricalHardware = ElectricalSystemHardware()

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

            val limelight = LimelightHardware()

            instance = Subsystems(
                    drivetrain, electricalHardware, driverHardware, operatorHardware, collectorRollers, storageBelt, barAdjustment, climberStow, climberWinch, controlPanel, controlWheel, shooter, feederRoller, turret, limelight
            )
        }

        fun concurrentInit() = scope.launch {
            supervisorScope {
                suspend fun <T> safeInit(producer: suspend () -> T): T? = try {
                    producer()
                } catch (t: Throwable) {
                    if (HardwareInit.crashOnFailure) throw t else null
                }

                suspend fun <R> initAsync(shouldInit: Boolean, producer: suspend () -> R) = async { if (shouldInit) producer() else null }

                val drivetrainAsync = async { DrivetrainComponent(DrivetrainHardware()) }
                val electricalAsync = async { ElectricalSystemHardware() }

                val driverAsync = async { DriverHardware() }
                val operatorAsync = async { OperatorHardware() }

                val collectorRollersAsync = initAsync(initCollectorRollers) { CollectorRollersComponent(CollectorRollersHardware()) }
                val storageBeltAsync = initAsync(initStorageBelt) { StorageBeltComponent(StorageBeltHardware()) }
                val controlPanelPivotAsync = initAsync(initControlPanelPivot) { ControlPanelPivotComponent(ControlPanelPivotHardware()) }
                val controlWheelAsync = initAsync(initControlWheel) { ControlWheelComponent(ControlWheelHardware()) }
                val barAdjustmentAsync = initAsync(initBarAdjustment) { BarAdjustmentComponent(BarAdjustmentHardware()) }
                val climberStowAsync = initAsync(initClimberStow) { ClimberStowComponent(ClimberStowHardware()) }
                val climberWinchAsync = initAsync(initClimberWinch) { ClimberWinchComponent(ClimberWinchHardware()) }
                val feederRollerAsync = initAsync(initFeederRoller) { FeederRollerComponent(FeederRollerHardware()) }
                val turretAsync = initAsync(initTurret) { TurretComponent(TurretHardware()) }
                val shooterAsync = initAsync(initShooter) { ShooterComponent(ShooterHardware()) }


                val limelightAsync = initAsync(initLimelight) { LimelightHardware() }

                instance = Subsystems(
                        drivetrainAsync.await(),
                        electricalAsync.await(),
                        driverAsync.await(),
                        operatorAsync.await(),
                        safeInit { collectorRollersAsync.await() },
                        safeInit { storageBeltAsync.await() },
                        safeInit { barAdjustmentAsync.await() },
                        safeInit { climberStowAsync.await() },
                        safeInit { climberWinchAsync.await() },
                        safeInit { controlPanelPivotAsync.await() },
                        safeInit { controlWheelAsync.await() },
                        safeInit { shooterAsync.await() },
                        safeInit { feederRollerAsync.await() },
                        safeInit { turretAsync.await() },
                        safeInit { limelightAsync.await() }
                )
            }
        }.also { runBlocking { it.join() } }
    }
}