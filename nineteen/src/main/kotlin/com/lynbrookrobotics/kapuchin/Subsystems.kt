package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.crashOnFailure
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.hal.HAL
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File

class Subsystems(val drivetrain: DrivetrainComponent,
                 val teleop: TeleopComponent,
                 val driver: DriverHardware,
                 val operator: OperatorHardware,
                 val leds: LedHardware?,

                 val lineScanner: LineScannerHardware?,
                 val collectorPivot: CollectorPivotComponent?,
                 val collectorRollers: CollectorRollersComponent?,
                 val collectorSlider: CollectorSliderComponent?,
                 val hook: HookComponent?,
                 val hookSlider: HookSliderComponent?,
                 val handoffPivot: HandoffPivotComponent?,
                 val handoffRollers: HandoffRollersComponent?,
                 val velcroPivot: VelcroPivotComponent?,
                 val lift: LiftComponent?,
                 val climber: ClimberComponent?,
                 val limelight: LimelightHardware?,
                 val electrical: ElectricalSystemHardware?
) : Named by Named("subsystems") {

    suspend fun teleop() {
        System.gc()
        HAL.observeUserProgramTeleop()
        runAll(
                { drivetrainTeleop(drivetrain, driver, limelight) }
        )
        System.gc()
    }

    suspend fun warmup() {
        System.gc()
        runAll(
                { drivetrain.warmup() }
        )
        System.gc()
    }

    val performance by pref(40, Percent)
    suspend fun followWaypoints() {
        val waypts = File("/tmp/journal.tsv").useLines { lns ->
            lns
                    .drop(1)
                    .map { it.split('\t') }
                    .map { it.map { tkn -> tkn.trim() } }
                    .map { Waypt(it[1].toDouble().Foot, it[2].toDouble().Foot) stampWith it[0].toDouble().Second }
                    .toList()
        }

        val traj = pathToTrajectory(
                waypts.map { (_, pt) -> pt },
                performance,
                drivetrain.maxSpeed,
                drivetrain.maxOmega
        )

        drivetrain.readJournal(2.Foot, 8.Inch, 5.FootPerSecondSquared, true, traj)

        freeze()
    }

    suspend fun limelightAlign() {
        if (limelight != null) {
            limelightAlign(drivetrain, limelight)
        }
    }

    companion object : Named by Named("subsystems") {

        var instance: Subsystems? = null
            private set

        val pneumaticTicker = ticker(Low, 100.milli(Second), "Pneumatic System Ticker")
        val uiBaselineTicker = ticker(Lowest, 500.milli(Second), "UI Baseline Ticker")

        fun concurrentInit() = runBlocking {
            val drivetrainAsync = async { DrivetrainComponent(DrivetrainHardware()) }

            val driverAsync = async { DriverHardware() }
            val operatorAsync = async { OperatorHardware() }
            val ledsAsync = async { LedHardware() }
            val teleopAsync = async {
                TeleopComponent(TeleopHardware(
                        driverAsync.await(), operatorAsync.await(), ledsAsync.await()
                ))
            }

            val lineScannerAsync = async { LineScannerHardware() }
            val collectorPivotAsync = async { CollectorPivotComponent(CollectorPivotHardware()) }
            val collectorRollersAsync = async { CollectorRollersComponent(CollectorRollersHardware()) }
            val collectorSliderAsync = async { CollectorSliderComponent(CollectorSliderHardware()) }
            val hookAsync = async { HookComponent(HookHardware()) }
            val hookSliderAsync = async { HookSliderComponent(HookSliderHardware()) }
            val handoffPivotAsync = async { HandoffPivotComponent(HandoffPivotHardware()) }
            val handoffRollersAsync = async { HandoffRollersComponent(HandoffRollersHardware()) }
            val velcroPivotAsync = async { VelcroPivotComponent(VelcroPivotHardware()) }
            val liftAsync = async { LiftComponent(LiftHardware()) }
            val climberAsync = async { ClimberComponent(ClimberHardware()) }
            val limelightAsync = async { LimelightHardware() }
            val electricalAsync = async { ElectricalSystemHardware() }

            suspend fun <T> t(f: suspend () -> T): T? = try {
                f()
            } catch (t: Throwable) {
                if (crashOnFailure) throw t else null
            }


            instance = Subsystems(
                    t { drivetrainAsync.await() }!!,

                    t { teleopAsync.await() }!!,
                    t { driverAsync.await() }!!,
                    t { operatorAsync.await() }!!,
                    t { ledsAsync.await() },

                    t { lineScannerAsync.await() },
                    t { collectorPivotAsync.await() },
                    t { collectorRollersAsync.await() },
                    t { collectorSliderAsync.await() },
                    t { hookAsync.await() },
                    t { hookSliderAsync.await() },
                    t { handoffPivotAsync.await() },
                    t { handoffRollersAsync.await() },
                    t { velcroPivotAsync.await() },
                    t { liftAsync.await() },
                    t { climberAsync.await() },
                    t { limelightAsync.await() },
                    t { electricalAsync.await() }
            )
        }

        fun sequentialInit() {
            fun <T> t(f: () -> T): T? = try {
                f()
            } catch (t: Throwable) {
                if (crashOnFailure) throw t else null
            }

            val driver = t { DriverHardware() }!!
            val operator = t { OperatorHardware() }!!
            val leds = t { LedHardware() }
            val teleop = t { TeleopComponent(TeleopHardware(driver, operator, leds)) }!!

            instance = Subsystems(
                    t { DrivetrainComponent(DrivetrainHardware()) }!!,
                    teleop,
                    driver,
                    operator,
                    leds,
                    t { LineScannerHardware() },
                    t { CollectorPivotComponent(CollectorPivotHardware()) },
                    t { CollectorRollersComponent(CollectorRollersHardware()) },
                    t { CollectorSliderComponent(CollectorSliderHardware()) },
                    t { HookComponent(HookHardware()) },
                    t { HookSliderComponent(HookSliderHardware()) },
                    t { HandoffPivotComponent(HandoffPivotHardware()) },
                    t { HandoffRollersComponent(HandoffRollersHardware()) },
                    t { VelcroPivotComponent(VelcroPivotHardware()) },
                    t { LiftComponent(LiftHardware()) },
                    t { ClimberComponent(ClimberHardware()) },
                    t { LimelightHardware() },
                    t { ElectricalSystemHardware() }
            )
        }
    }
}