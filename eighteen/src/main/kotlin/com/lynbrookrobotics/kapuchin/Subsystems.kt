package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainHardware
import kotlinx.coroutines.experimental.runBlocking

data class Subsystems(
        val forks: ForksComponent, val hooks: HooksComponent, val winch: WinchComponent,
        val clamp: ClampComponent, val pivot: PivotComponent, val rollers: RollersComponent,
        val drivetrain: DrivetrainComponent, val lift: LiftComponent,

        val driverHardware: DriverHardware
) {
    companion object : Named("Subsystems Initializer") {
        fun concurrentInit() = runBlocking {
            val electricalHardw = init(::ElectricalSystemHardware)
            val driverHardw = init(::DriverHardware)

            val forksHardw = init(::ForksHardware)
            val hooksHardw = init(::HooksHardware)
            val winchHardw = init(::WinchHardware)
            val clampHardw = init(::ClampHardware)
            val pivotHardw = init(::PivotHardware)
            val rollersHardw = init(::RollersHardware)
            val drivetrainHardw = init(::DrivetrainHardware)
            val liftHardw = init(::LiftHardware)

            val forksComp = init(::ForksComponent with forksHardw)
            val hooksComp = init(::HooksComponent with hooksHardw)
            val winchComp = init(::WinchComponent with winchHardw)
            val clampComp = init(::ClampComponent with clampHardw)
            val pivotComp = init(::PivotComponent with pivotHardw)
            val rollersComp = init(::RollersComponent with rollersHardw with electricalHardw)
            val drivetrainComp = init(::DrivetrainComponent with drivetrainHardw)
            val liftComp = init(::LiftComponent with liftHardw)

            Subsystems(
                    forks = forksComp.await(),
                    hooks = hooksComp.await(),
                    winch = winchComp.await(),
                    clamp = clampComp.await(),
                    pivot = pivotComp.await(),
                    rollers = rollersComp.await(),
                    drivetrain = drivetrainComp.await(),
                    lift = liftComp.await(),
                    driverHardware = driverHardw.await()
            )
        }

        fun init(): Subsystems {
            val electricalSystemHardware = ElectricalSystemHardware()
            return Subsystems(
                    forks = ForksComponent(ForksHardware()),
                    hooks = HooksComponent(HooksHardware()),
                    winch = WinchComponent(WinchHardware()),
                    clamp = ClampComponent(ClampHardware()),
                    pivot = PivotComponent(PivotHardware()),
                    rollers = RollersComponent(RollersHardware(), electricalSystemHardware),
                    drivetrain = DrivetrainComponent(DrivetrainHardware()),
                    lift = LiftComponent(LiftHardware()),
                    driverHardware = DriverHardware()
            )
        }
    }
}