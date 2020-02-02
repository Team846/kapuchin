package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.crashOnFailure
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.control_panel.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
<<<<<<< HEAD
=======
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
>>>>>>> teleop2020
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.hal.HAL
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.RobotController
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.system.exitProcess

class Subsystems(val drivetrain: DrivetrainComponent,
                 val electrical: ElectricalSystemHardware,

                 val driver: DriverHardware,
                 val operator: OperatorHardware,
                 val rumble: RumbleComponent,

                 val collectorRollers: CollectorRollersComponent?,
                 val intakePivot: IntakePivotComponent?,
                 val climberWinch: ClimberWinchComponent?,
                 val climberPivot: ClimberPivotComponent?,
<<<<<<< HEAD

                 val flywheel: FlywheelComponent?,
                 val hood: HoodComponent?,
=======
                 val carousel: CarouselComponent?,
                 val controlPanelPivot: ControlPanelPivotComponent?,
                 val controlPanelSpinner: ControlPanelSpinnerComponent?,
                 val feederRoller: FeederRollerComponent?,
                 val shooter: ShooterComponent?,
                 val turret: TurretComponent?,
>>>>>>> teleop2020

                 val limelight: LimelightComponent?
) : Named by Named("Subsystems") {

    suspend fun teleop() {
        System.gc()
        HAL.observeUserProgramTeleop()
        runAll(
                { drivetrainTeleop() },
                { climberTeleop() },
                { intakeTeleop() },
                { controlPanelTeleop() },
                { shooterTeleop() },
                { rumbleTeleop() },
                { limelight?.autoZoom() }
        )
        System.gc()
    }

    suspend fun warmup() {
        System.gc()
        runAll(
                { drivetrain.warmup() },
                {
                    while (isActive) {
                        delay(0.3.Second)
                        if (RobotController.getUserButton()) exitProcess(0)
                    }
                }
        )
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

                exitProcess(1)
            }
        }

        private val initCollectorRollers by pref(true)
<<<<<<< HEAD
        private val initClimberWinch by pref(false)
        private val initClimberIntake by pref(false)
        private val initFlywheel by pref(true)
        private val initHood by pref(true)
=======
        private val initIntakePivot by pref(true)
        private val initClimberWinch by pref(true)
        private val initClimberPivot by pref(true)
        private val initCarousel by pref(true)
        private val initControlPanelPivot by pref(true)
        private val initControlPanelSpinner by pref(true)
        private val initFeederRoller by pref(true)
        private val initShooter by pref(true)
        private val initTurret by pref(true)
>>>>>>> teleop2020
        private val initLimelight by pref(true)

        var instance: Subsystems? = null
            private set

        val pneumaticTicker = ticker(Medium, 50.milli(Second), "Pneumatic System Ticker")
        val uiBaselineTicker = ticker(Lowest, 500.milli(Second), "UI Baseline Ticker")

        fun concurrentInit() = scope.launch {
            supervisorScope {
                suspend fun <T> safeInit(producer: suspend () -> T): T? = try {
                    producer()
                } catch (t: Throwable) {
                    if (crashOnFailure) throw t else null
                }

                suspend fun <R> initAsync(shouldInit: Boolean, producer: suspend () -> R) = async { if (shouldInit) producer() else null }

                val drivetrainAsync = async { DrivetrainComponent(DrivetrainHardware()) }
                val electricalAsync = async { ElectricalSystemHardware() }

                val driverAsync = async { DriverHardware() }
                val operatorAsync = async { OperatorHardware() }
                val rumbleAsync = async { RumbleComponent(RumbleHardware(driverAsync.await(), operatorAsync.await())) }

                val collectorRollersAsync = initAsync(initCollectorRollers) { CollectorRollersComponent(CollectorRollersHardware()) }
                val climberWinchAsync = initAsync(initClimberWinch) { ClimberWinchComponent(ClimberWinchHardware()) }
<<<<<<< HEAD
                val climberIntakeAsync = initAsync(initClimberIntake) { ClimberIntakeComponent(ClimberIntakeHardware()) }

                val flywheelAsync = initAsync(initFlywheel) { FlywheelComponent(FlywheelHardware()) }
                val hoodAsync = initAsync(initHood) { HoodComponent(HoodHardware) }

                val limelightAsync = initAsync(initLimelight) { LimelightComponent(LimelightHardware()) }
=======
                val climberPivotAsync = initAsync(initClimberPivot) { ClimberPivotComponent(ClimberPivotHardware()) }
                val intakePivotAsync = initAsync(initIntakePivot) { IntakePivotComponent(IntakePivotHardware()) }
                val carouselAsync = initAsync(initCarousel) { CarouselComponent(CarouselHardware()) }
                val controlPanelPivotAsync = initAsync(initControlPanelPivot) { ControlPanelPivotComponent(ControlPanelPivotHardware()) }
                val controlPanelSpinnerAsync = initAsync(initControlPanelSpinner) { ControlPanelSpinnerComponent(ControlPanelSpinnerHardware()) }
                val feederRollerAsync = initAsync(initFeederRoller) { FeederRollerComponent(FeederRollerHardware()) }
                val shooterAsync = initAsync(initShooter) { ShooterComponent(ShooterHardware()) }
                val turretAsync = initAsync(initTurret) { TurretComponent(TurretHardware()) }
                val limelightAsync = initAsync(initLimelight) { LimelightComponent(LimelightHardware()) }

>>>>>>> teleop2020

                instance = Subsystems(
                        drivetrainAsync.await(),
                        electricalAsync.await(),

                        driverAsync.await(),
                        operatorAsync.await(),
                        rumbleAsync.await(),

                        safeInit { collectorRollersAsync.await() },
                        safeInit { intakePivotAsync.await() },
                        safeInit { climberWinchAsync.await() },
<<<<<<< HEAD
                        safeInit { climberInitAsync.await() },
                        safeInit { flywheelAsync.await() },
                        safeInit { hoodAsync.await() },
=======
                        safeInit { climberPivotAsync.await() },
                        safeInit { carouselAsync.await() },
                        safeInit { controlPanelPivotAsync.await() },
                        safeInit { controlPanelSpinnerAsync.await() },
                        safeInit { feederRollerAsync.await() },
                        safeInit { shooterAsync.await() },
                        safeInit { turretAsync.await() },

>>>>>>> teleop2020
                        safeInit { limelightAsync.await() }
                )
            }
        }.also { runBlocking { it.join() } }

        fun sequentialInit() {
            fun <T> safeInit(f: () -> T): T? = try {
                f()
            } catch (t: Throwable) {
                if (crashOnFailure) throw t else null
            }

            fun <R> initOrNull(shouldInit: Boolean, producer: () -> R) = if (shouldInit) producer() else null

            val driver = DriverHardware()
            val operator = OperatorHardware()
            val rumble = RumbleComponent(RumbleHardware(driver, operator))

            instance = Subsystems(
                    DrivetrainComponent(DrivetrainHardware()),
                    ElectricalSystemHardware(),
                    driver,
                    operator,
                    rumble,
                    initOrNull(initCollectorRollers) { safeInit { CollectorRollersComponent(CollectorRollersHardware()) } },
                    initOrNull(initIntakePivot) { safeInit { IntakePivotComponent(IntakePivotHardware()) } },
                    initOrNull(initClimberWinch) { safeInit { ClimberWinchComponent(ClimberWinchHardware()) } },
<<<<<<< HEAD
                    initOrNull(initClimberIntake) { safeInit { ClimberIntakeComponent(ClimberIntakeHardware()) } },
                    initOrNull(initFlywheel) { safeInit { FlywheelComponent(FlywheelHardware()) } },
                    initOrNull(initHood) { safeInit { HoodComponent(HoodHardware()) } },
=======
                    initOrNull(initClimberPivot) { safeInit { ClimberPivotComponent(ClimberPivotHardware()) } },
                    initOrNull(initCarousel) { safeInit { CarouselComponent(CarouselHardware()) } },
                    initOrNull(initControlPanelPivot) { safeInit { ControlPanelPivotComponent(ControlPanelPivotHardware()) } },
                    initOrNull(initControlPanelSpinner) { safeInit { ControlPanelSpinnerComponent(ControlPanelSpinnerHardware()) } },
                    initOrNull(initFeederRoller) { safeInit { FeederRollerComponent(FeederRollerHardware()) } },
                    initOrNull(initShooter) { safeInit { ShooterComponent(ShooterHardware()) } },
                    initOrNull(initTurret) { safeInit { TurretComponent(TurretHardware()) } },
>>>>>>> teleop2020
                    initOrNull(initLimelight) { safeInit { LimelightComponent(LimelightHardware()) } }
            )
        }
    }
}