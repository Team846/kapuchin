package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.crashOnFailure
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.slider.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
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

class Subsystems(val drivetrain: DrivetrainComponent,
                 val electrical: ElectricalSystemHardware,

                 val driver: DriverHardware,
                 val operator: OperatorHardware,
                 val rumble: RumbleComponent,
                 val leds: LedComponent?,

                 val lineScanner: LineScannerHardware,
                 val collectorPivot: CollectorPivotComponent?,
                 val collectorRollers: CollectorRollersComponent?,
                 val collectorSlider: CollectorSliderComponent?,
                 val hook: HookComponent?,
                 val hookSlider: HookSliderComponent?,
                 val lift: LiftComponent?,
                 val climber: ClimberComponent?,
                 val limelight: LimelightComponent?
) : Named by Named("Subsystems") {

    suspend fun teleop() {
        System.gc()
        HAL.observeUserProgramTeleop()
        runAll(
                { drivetrainTeleop() },
                { intakeTeleop() },
                { liftTeleop() },
                { climberTeleop() },
                { rumbleTeleop() },
                { limelight?.autoZoom() }
        )
        System.gc()
    }

    suspend fun warmup() {
        System.gc()
        runAll(
                { drivetrain.warmup() },
                { limelight?.autoZoom() },
                {
                    while (isActive) {
                        delay(0.3.Second)
                        if (RobotController.getUserButton()) System.exit(0)
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
                        .getResourceAsStream("com/lynbrookrobotics/kapuchin/configbackups/networktables.ini")
                        .copyTo(File(ntPath).outputStream())
                File("$ntPath.bak").delete()

                System.exit(1)
            }
        }

        private val initLeds by pref(true)
        private val initCollectorPivot by pref(true)
        private val initCollectorRollers by pref(true)
        private val initCollectorSlider by pref(false)
        private val initHook by pref(true)
        private val initHookSlider by pref(true)
        private val initHandoffPivot by pref(false)
        private val initHandoffRollers by pref(false)
        private val initVelcroPivot by pref(false)
        private val initLift by pref(true)
        private val initClimber by pref(false)
        private val initLimelight by pref(true)

        var instance: Subsystems? = null
            private set

        val pneumaticTicker = ticker(Medium, 50.milli(Second), "Pneumatic System Ticker")
        val uiBaselineTicker = ticker(Lowest, 500.milli(Second), "UI Baseline Ticker")

        fun concurrentInit() = scope.launch {
            supervisorScope {
                suspend fun <T> t(f: suspend () -> T): T? = try {
                    f()
                } catch (t: Throwable) {
                    if (crashOnFailure) throw t else null
                }

                suspend fun <R> i(b: Boolean, f: suspend () -> R) = async { if (b) f() else null }

                val drivetrainAsync = async { DrivetrainComponent(DrivetrainHardware()) }
                val electricalAsync = async { ElectricalSystemHardware() }

                val driverAsync = async { DriverHardware() }
                val operatorAsync = async { OperatorHardware() }
                val rumbleAsync = async { RumbleComponent(RumbleHardware(driverAsync.await(), operatorAsync.await())) }
                val lineScannerAsync = async { LineScannerHardware() }

                val ledsAsync = i(initLeds) { LedComponent(LedHardware(driverAsync.await())) }
                val collectorPivotAsync = i(initCollectorPivot) { CollectorPivotComponent(CollectorPivotHardware()) }
                val collectorRollersAsync = i(initCollectorRollers) { CollectorRollersComponent(CollectorRollersHardware()) }
                val collectorSliderAsync = i(initCollectorSlider) { CollectorSliderComponent(CollectorSliderHardware()) }
                val hookAsync = i(initHook) { HookComponent(HookHardware()) }
                val hookSliderAsync = i(initHookSlider) { HookSliderComponent(HookSliderHardware()) }
                val liftAsync = i(initLift) { LiftComponent(LiftHardware()) }
                val climberAsync = i(initClimber) { ClimberComponent(ClimberHardware()) }
                val limelightAsync = i(initLimelight) { LimelightComponent(LimelightHardware()) }

                instance = Subsystems(
                        drivetrainAsync.await(),
                        electricalAsync.await(),

                        driverAsync.await(),
                        operatorAsync.await(),
                        rumbleAsync.await(),
                        t { ledsAsync.await() },

                        lineScannerAsync.await(),
                        t { collectorPivotAsync.await() },
                        t { collectorRollersAsync.await() },
                        t { collectorSliderAsync.await() },
                        t { hookAsync.await() },
                        t { hookSliderAsync.await() },
                        t { liftAsync.await() },
                        t { climberAsync.await() },
                        t { limelightAsync.await() }
                )
            }
        }.also { runBlocking { it.join() } }

        fun sequentialInit() {
            fun <T> t(f: () -> T): T? = try {
                f()
            } catch (t: Throwable) {
                if (crashOnFailure) throw t else null
            }

            fun <R> i(b: Boolean, f: () -> R) = if (b) f() else null

            val driver = DriverHardware()
            val operator = OperatorHardware()
            val rumble = RumbleComponent(RumbleHardware(driver, operator))
            val leds = i(initLeds) { t { LedComponent(LedHardware(driver)) } }

            instance = Subsystems(
                    DrivetrainComponent(DrivetrainHardware()),
                    ElectricalSystemHardware(),
                    driver,
                    operator,
                    rumble,
                    leds,
                    LineScannerHardware(),
                    i(initCollectorPivot) { t { CollectorPivotComponent(CollectorPivotHardware()) } },
                    i(initCollectorRollers) { t { CollectorRollersComponent(CollectorRollersHardware()) } },
                    i(initCollectorSlider) { t { CollectorSliderComponent(CollectorSliderHardware()) } },
                    i(initHook) { t { HookComponent(HookHardware()) } },
                    i(initHookSlider) { t { HookSliderComponent(HookSliderHardware()) } },
                    i(initLift) { t { LiftComponent(LiftHardware()) } },
                    i(initClimber) { t { ClimberComponent(ClimberHardware()) } },
                    i(initLimelight) { t { LimelightComponent(LimelightHardware()) } }
            )
        }
    }
}