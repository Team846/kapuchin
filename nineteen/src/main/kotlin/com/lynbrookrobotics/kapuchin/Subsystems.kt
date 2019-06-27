package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.crashOnFailure
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.control.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.feedback.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
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

class Subsystems(
        val driver: Driver,
        val operator: Operator,

        val drivetrain: Drivetrain,
        val lift: Lift?,
        val climber: Climber?,

        val collectorPivot: CollectorPivot?,
        val collectorRollers: CollectorRollers?,
        val collectorSlider: CollectorSlider?,
        val hook: Hook?,
        val hookSlider: HookSlider?,

        val leds: Leds?,
        val rumble: Rumble,

        val electrical: Electrical,
        val limelight: Limelight?,
        val lineScanner: LineScanner
) : Named by Named("Subsystems") {

    suspend fun teleop() {
        System.gc()
        HAL.observeUserProgramTeleop()
        runAll(
                { drivetrainTeleop() },
                { intakeTeleop() },
                { liftTeleop() },
                { climberTeleop() },
                { rumbleTeleop() }
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


        private val initLift by pref(true)
        private val initClimber by pref(true)
        private val initLeds by pref(true)
        private val initCollectorPivot by pref(true)
        private val initCollectorRollers by pref(true)
        private val initCollectorSlider by pref(true)
        private val initHook by pref(true)
        private val initHookSlider by pref(true)
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

                val driverAsync = async { Driver() }
                val operatorAsync = async { Operator() }

                val drivetrainAsync = async { Drivetrain(DrivetrainHardware()) }
                val liftAsync = i(initLift) { Lift(LiftHardware()) }
                val climberAsync = i(initClimber) { Climber(ClimberHardware()) }

                val collectorPivotAsync = i(initCollectorPivot) { CollectorPivot(CollectorPivotHardware()) }
                val collectorRollersAsync = i(initCollectorRollers) { CollectorRollers(CollectorRollersHardware()) }
                val collectorSliderAsync = i(initCollectorSlider) { CollectorSlider(CollectorSliderHardware()) }
                val hookAsync = i(initHook) { Hook(HookHardware()) }
                val hookSliderAsync = i(initHookSlider) { HookSlider(HookSliderHardware()) }

                val ledsAsync = i(initLeds) { Leds(LedsHardware(driverAsync.await())) }
                val rumbleAsync = async { Rumble(RumbleHardware(driverAsync.await(), operatorAsync.await())) }

                val electricalAsync = async { Electrical() }
                val limelightAsync = i(initLimelight) { Limelight() }
                val lineScannerAsync = async { LineScanner() }

                instance = Subsystems(
                        driverAsync.await(),
                        operatorAsync.await(),

                        drivetrainAsync.await(),
                        t { liftAsync.await() },
                        t { climberAsync.await() },

                        t { collectorPivotAsync.await() },
                        t { collectorRollersAsync.await() },
                        t { collectorSliderAsync.await() },
                        t { hookAsync.await() },
                        t { hookSliderAsync.await() },

                        t { ledsAsync.await() },
                        rumbleAsync.await(),

                        electricalAsync.await(),
                        t { limelightAsync.await() },
                        lineScannerAsync.await()
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

            val driver = Driver()
            val operator = Operator()
            val rumble = Rumble(RumbleHardware(driver, operator))
            val leds = i(initLeds) { t { Leds(LedsHardware(driver)) } }

            instance = Subsystems(
                    driver,
                    operator,

                    Drivetrain(DrivetrainHardware()),
                    i(initLift) { t { Lift(LiftHardware()) } },
                    i(initClimber) { t { Climber(ClimberHardware()) } },

                    i(initCollectorPivot) { t { CollectorPivot(CollectorPivotHardware()) } },
                    i(initCollectorRollers) { t { CollectorRollers(CollectorRollersHardware()) } },
                    i(initCollectorSlider) { t { CollectorSlider(CollectorSliderHardware()) } },
                    i(initHook) { t { Hook(HookHardware()) } },
                    i(initHookSlider) { t { HookSlider(HookSliderHardware()) } },

                    leds,
                    rumble,

                    Electrical(),
                    i(initLimelight) { t { Limelight() } },
                    LineScanner()
            )
        }
    }
}