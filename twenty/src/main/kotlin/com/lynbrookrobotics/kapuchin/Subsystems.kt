package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.crashOnFailure
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.subsystems.storage.*
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

                 val climberChode: ClimberChodeComponent?,
                 val climberPivot: ClimberPivotComponent?,
                 val climberWinch: ClimberWinchComponent?,
                 val controlPanelPivot: ControlPanelPivotComponent?,
                 val controlPanelSpinner: ControlPanelSpinnerComponent?,
                 val intakeSlider: IntakeSliderComponent?,
                 val intakeRollers: IntakeRollersComponent?,
                 val limelight: LimelightComponent?,
                 val flywheel: FlywheelComponent?,
                 val shooterHood: ShooterHoodComponent?,
                 val turret: TurretComponent?,
                 val carousel: CarouselComponent?,
                 val feederRoller: FeederRollerComponent?
) : Named by Named("Subsystems") {

    suspend fun teleop() {
        System.gc()
        HAL.observeUserProgramTeleop()
        runAll(
                { climberTeleop() },
                { controlPanelTeleop() },
                { intakeTeleop() },
                { shooterTeleop() },
                { drivetrain.teleop(driver) },
                { limelight?.autoZoom() }
        )
        System.gc()
    }

    suspend fun warmup() {
        System.gc()
        runAll(
                { limelight?.autoZoom() },
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

        private val initClimberChode by pref(true)
        private val initClimberPivot by pref(true)
        private val initClimberWinch by pref(true)
        private val initControlPanelPivot by pref(true)
        private val initControlPanelSpinner by pref(true)
        private val initIntakeSlider by pref(true)
        private val initIntakeRollers by pref(true)
        private val initLimelight by pref(true)
        private val initFlywheel by pref(true)
        private val initShooterHood by pref(true)
        private val initTurret by pref(true)
        private val initCarousel by pref(true)
        private val initFeederRoller by pref(true)

        var instance: Subsystems? = null
            private set

        val pneumaticTicker = ticker(Medium, 50.milli(Second), "Pneumatic System Ticker")
        val uiBaselineTicker = ticker(Lowest, 500.milli(Second), "UI Baseline Ticker")
        fun sharedTickerTiming() = error("Subsystem should use shared ticker!")

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

                val climberChodeAsync = initAsync(initClimberChode) { ClimberChodeComponent(ClimberChodeHardware()) }
                val climberPivotAsync = initAsync(initClimberPivot) { ClimberPivotComponent(ClimberPivotHardware()) }
                val climberWinchAsync = initAsync(initClimberWinch) { ClimberWinchComponent(ClimberWinchHardware()) }
                val controlPanelPivotAsync = initAsync(initControlPanelPivot) { ControlPanelPivotComponent(ControlPanelPivotHardware()) }
                val controlPanelSpinnerAsync = initAsync(initControlPanelSpinner) { ControlPanelSpinnerComponent(ControlPanelSpinnerHardware()) }
                val intakeSliderAsync = initAsync(initIntakeSlider) { IntakeSliderComponent(IntakeSliderHardware()) }
                val intakeRollersAsync = initAsync(initIntakeRollers) { IntakeRollersComponent(IntakeRollersHardware()) }
                val limelightAsync = initAsync(initLimelight) { LimelightComponent(LimelightHardware()) }
                val flywheelAsync = initAsync(initFlywheel) { FlywheelComponent(FlywheelHardware()) }
                val shooterHoodAsync = initAsync(initShooterHood) { ShooterHoodComponent(ShooterHoodHardware()) }
                val turretAsync = initAsync(initTurret) { TurretComponent(TurretHardware()) }
                val carouselAsync = initAsync(initCarousel) { CarouselComponent(CarouselHardware()) }
                val feederRollerAsync = initAsync(initFeederRoller) { FeederRollerComponent(FeederRollerHardware()) }

                instance = Subsystems(
                        drivetrainAsync.await(),
                        electricalAsync.await(),
                        driverAsync.await(),
                        operatorAsync.await(),
                        rumbleAsync.await(),

                        safeInit { climberChodeAsync.await() },
                        safeInit { climberPivotAsync.await() },
                        safeInit { climberWinchAsync.await() },
                        safeInit { controlPanelPivotAsync.await() },
                        safeInit { controlPanelSpinnerAsync.await() },
                        safeInit { intakeSliderAsync.await() },
                        safeInit { intakeRollersAsync.await() },
                        safeInit { limelightAsync.await() },
                        safeInit { flywheelAsync.await() },
                        safeInit { shooterHoodAsync.await() },
                        safeInit { turretAsync.await() },
                        safeInit { carouselAsync.await() },
                        safeInit { feederRollerAsync.await() }
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

                    initOrNull(initClimberChode) { safeInit { ClimberChodeComponent(ClimberChodeHardware()) } },
                    initOrNull(initClimberPivot) { safeInit { ClimberPivotComponent(ClimberPivotHardware()) } },
                    initOrNull(initClimberWinch) { safeInit { ClimberWinchComponent(ClimberWinchHardware()) } },
                    initOrNull(initControlPanelPivot) { safeInit { ControlPanelPivotComponent(ControlPanelPivotHardware()) } },
                    initOrNull(initControlPanelSpinner) { safeInit { ControlPanelSpinnerComponent(ControlPanelSpinnerHardware()) } },
                    initOrNull(initIntakeSlider) { safeInit { IntakeSliderComponent(IntakeSliderHardware()) } },
                    initOrNull(initIntakeRollers) { safeInit { IntakeRollersComponent(IntakeRollersHardware()) } },
                    initOrNull(initLimelight) { safeInit { LimelightComponent(LimelightHardware()) } },
                    initOrNull(initFlywheel) { safeInit { FlywheelComponent(FlywheelHardware()) } },
                    initOrNull(initShooterHood) { safeInit { ShooterHoodComponent(ShooterHoodHardware()) } },
                    initOrNull(initTurret) { safeInit { TurretComponent(TurretHardware()) } },
                    initOrNull(initCarousel) { safeInit { CarouselComponent(CarouselHardware()) } },
                    initOrNull(initFeederRoller) { safeInit { FeederRollerComponent(FeederRollerHardware()) } }
            )
        }
    }
}