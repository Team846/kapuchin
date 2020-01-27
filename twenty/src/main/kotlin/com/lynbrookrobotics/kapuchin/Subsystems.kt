package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.hal.HAL
import edu.wpi.first.networktables.NetworkTableInstance
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import java.io.File


class Subsystems(val drivetrain: DrivetrainComponent,

                 val electrical: ElectricalSystemHardware,
                 val driver: DriverHardware,
                 val operator: OperatorHardware,


                 val collectorRollers: CollectorRollersComponent?,
                 val carousel: CarouselComponent?,
                 val climberBarAdjustment: ClimberBarAdjustmentComponent?,
                 val climberStow: ClimberStowComponent?,
                 val climberWinch: ClimberWinchComponent?,
                 val controlPanelPivot: ControlPanelPivotComponent?,
                 val controlPanelSpinner: ControlPanelSpinnerComponent?,
                 val shooter: ShooterComponent?,
                 val feederRoller: FeederRollerComponent?,
                 val turret: TurretComponent?,
                 val intakePivot: IntakePivotComponent?,

                 val limelight: LimelightComponent?
) : Named by Named("Subsystems") {

    suspend fun teleop() {
        System.gc()
        HAL.observeUserProgramTeleop()
        runAll(

        )
        System.gc()
    }

    fun warmup() {
        System.gc()
    }

    companion object : Named by Named("Subsystems") {

        private val isCorrupted by pref(true)

        init {
            if (isCorrupted) {
                log(Error) { "The config seems to be corrupted. Attempting restoration." }
                NetworkTableInstance.getDefault().stopServer()

                val ntPath = "/home/lvuser/networktables.ini"

                Thread.currentThread()
                        .contextClassLoader
                        .getResourceAsStream("com/lynbrookrobotics/kapuchin/configbackups/networktables.ini")!!
                        .copyTo(File(ntPath).outputStream())
                File("$ntPath.bak").delete()

                System.exit(1)
            }
        }

        private val initCollectorRollers by pref(true)
        private val initCarousel by pref(true)
        private val initControlPanelPivot by pref(true)
        private val initControlPanelSpinner by pref(false)
        private val initClimberBarAdjustment by pref(false)
        private val initClimberStow by pref(false)
        private val initClimberWinch by pref(true)
        private val initFeederRoller by pref(false)
        private val initTurret by pref(true)
        private val initShooter by pref(true)
        private val initIntakePivot by pref (false)

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

            val carouselHardware = CarouselHardware()
            val carousel = CarouselComponent(carouselHardware)

            val climberBarAdjustmentHardware = ClimberBarAdjustmentHardware()
            val climberBarAdjustment = ClimberBarAdjustmentComponent(climberBarAdjustmentHardware)

            val climberStowHardware = ClimberStowHardware()
            val climberStow = ClimberStowComponent(climberStowHardware)

            val climberWinchHardware = ClimberWinchHardware()
            val climberWinch = ClimberWinchComponent(climberWinchHardware)

            val controlPanelHardware = ControlPanelPivotHardware()
            val controlPanel = ControlPanelPivotComponent(controlPanelHardware)

            val controlPanelSpinnerHardware = ControlPanelSpinnerHardware()
            val controlPanelSpinner = ControlPanelSpinnerComponent(controlPanelSpinnerHardware)

            val shooterHardware = ShooterHardware()
            val shooter = ShooterComponent(shooterHardware)

            val feederRollerHardware = FeederRollerHardware()
            val feederRoller = FeederRollerComponent(feederRollerHardware)

            val turretHardware = TurretHardware()
            val turret = TurretComponent(turretHardware)

            val limelightHardware = LimelightHardware()
            val limelight = LimelightComponent(limelightHardware)

            val intakePivotHardware = IntakePivotHardware()
            val intakePivot = IntakePivotComponent(intakePivotHardware)



            instance = Subsystems(
                    drivetrain, electricalHardware, driverHardware, operatorHardware, collectorRollers, carousel, climberBarAdjustment, climberStow, climberWinch, controlPanel, controlPanelSpinner, shooter, feederRoller, turret,  intakePivot, limelight
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
                val carouselAsync = initAsync(initCarousel) { CarouselComponent(CarouselHardware()) }
                val controlPanelPivotAsync = initAsync(initControlPanelPivot) { ControlPanelPivotComponent(ControlPanelPivotHardware()) }
                val controlPanelSpinnerAsync = initAsync(initControlPanelSpinner) { ControlPanelSpinnerComponent(ControlPanelSpinnerHardware()) }
                val climberBarAdjustmentAsync = initAsync(initClimberBarAdjustment) { ClimberBarAdjustmentComponent(ClimberBarAdjustmentHardware()) }
                val climberStowAsync = initAsync(initClimberStow) { ClimberStowComponent(ClimberStowHardware()) }
                val climberWinchAsync = initAsync(initClimberWinch) { ClimberWinchComponent(ClimberWinchHardware()) }
                val feederRollerAsync = initAsync(initFeederRoller) { FeederRollerComponent(FeederRollerHardware()) }
                val turretAsync = initAsync(initTurret) { TurretComponent(TurretHardware()) }
                val shooterAsync = initAsync(initShooter) { ShooterComponent(ShooterHardware()) }
                val intakePivotAsync = initAsync(initIntakePivot) { IntakePivotComponent(IntakePivotHardware()) }


                val limelightAsync = initAsync(initLimelight) { LimelightComponent(LimelightHardware()) }

                instance = Subsystems(
                        drivetrainAsync.await(),
                        electricalAsync.await(),
                        driverAsync.await(),
                        operatorAsync.await(),
                        safeInit { collectorRollersAsync.await() },
                        safeInit { carouselAsync.await() },
                        safeInit { climberBarAdjustmentAsync.await() },
                        safeInit { climberStowAsync.await() },
                        safeInit { climberWinchAsync.await() },
                        safeInit { controlPanelPivotAsync.await() },
                        safeInit { controlPanelSpinnerAsync.await() },
                        safeInit { shooterAsync.await() },
                        safeInit { feederRollerAsync.await() },
                        safeInit { turretAsync.await() },
                        safeInit { intakePivotAsync.await()},
                        safeInit { limelightAsync.await() }



                )
            }
        }.also { runBlocking { it.join() } }
    }
}