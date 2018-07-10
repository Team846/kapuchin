package com.lynbrookrobotics.kapuchin.tests.hardware

import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.`is greater than?`
import com.lynbrookrobotics.kapuchin.tests.hardware.TC
import com.lynbrookrobotics.kapuchin.tests.hardware.TSH
import com.lynbrookrobotics.kapuchin.timing.checkInSync
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Second
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test

class SensorTest {

    private class SensorTestSH : TSH<SensorTestSH, SensorTestC>("SensorTest Hardware") {
        val sensorA = sensor { Math.random() stampWith currentTime }
        val sensorB = sensor { Math.random() stampWith currentTime }
        override val period = 0.5.Second
        override val syncThreshold = period / 10
    }

    private object SensorTestC : TC<SensorTestC, SensorTestSH>(SensorTestSH())

    @Test(timeout = 6 * 1000)
    fun `sensors read on tick are in sync`() = runBlocking {
        val name = "sensors read on tick are in sync"
        SensorTestC.run {
            val a by hardware.sensorA.readOnTick.withStamps
            val b by hardware.sensorB.readOnTick.withStamps
            var runs = 10
            runRoutine(name) {
                checkInSync(hardware.syncThreshold, a, b) `is equal to?` true
                currentTime `is greater than?` a.stamp
                currentTime `is greater than?` b.stamp
                name.takeIf { runs-- > 0 }
            }
        }
    }

    @Test(timeout = 6 * 1000)
    fun `sensors read eagerly are eager and efficient`() = runBlocking {
        val name = "sensors read eagerly are eager and efficient"
        SensorTestC.run {
            val a by hardware.sensorA.readEagerly.withStamps
            val b by hardware.sensorB.readEagerly.withStamps
            var runs = 10
            runRoutine(name) {
                val a1 = a
                val b1 = b
                val a2 = a
                val b2 = b

                b2 `is equal to?` b1
                a2.stamp `is greater than?` b1.stamp
                a2 `is equal to?` a1

                name.takeIf { runs-- > 0 }
            }
        }
    }

    @Test(timeout = 6 * 1000)
    fun `sensors are read efficiently`() = runBlocking {
        val name = "sensors are read efficiently"
        SensorTestC.run {
            val a1 by hardware.sensorA.readOnTick.withStamps
            val a2 by hardware.sensorA.readEagerly.withStamps
            val b1 by hardware.sensorB.readOnTick.withStamps
            val b2 by hardware.sensorB.readEagerly.withStamps
            var runs = 10
            runRoutine(name) {
                a1 `is equal to?` a2
                b1 `is equal to?` b2
                name.takeIf { runs-- > 0 }
            }
        }
    }
}