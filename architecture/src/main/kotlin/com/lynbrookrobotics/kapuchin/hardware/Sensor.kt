package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Sensor initialization domain-specific language
 *
 * Helps manage concurrent sensor use across multiple subsystems
 *
 * @author Kunal
 * @see Grapher
 * @see RobotHardware
 * @see SubsystemHardware
 * @see HardwareInit
 * @see DelegateProvider
 *
 * @param Input type of sensor data being read
 */
class Sensor<Input> internal constructor(internal val read: (Time) -> TimeStamped<Input>) {

    internal var value: TimeStamped<Input>? = null
    fun optimizedRead(atTime: Time, syncThreshold: Time) =
            value
                    ?.takeIf { it.x in atTime `±` syncThreshold }
                    ?: blockingMutex(this) {
                        read(atTime).also { value = it }
                    }

    class UpdateSource<Input>(
            private val forSensor: Sensor<Input>,
            private val startUpdates: (Sensor<Input>) -> Unit = { _ -> },
            private val getValue: (Sensor<Input>) -> TimeStamped<Input> = {
                it.value ?: it.optimizedRead(currentTime, 0.Second)
            }
    ) {
        /**
         * Get sensor data only
         */
        val withoutStamps
            get() = object : DelegateProvider<Any?, Input> {
                override fun provideDelegate(thisRef: Any?, prop: KProperty<*>) = object : ReadOnlyProperty<Any?, Input> {
                    override fun getValue(thisRef: Any?, property: KProperty<*>) = getValue(forSensor).y
                }.also { startUpdates(forSensor) }
            }

        /**
         * Get sensor data and timestamps
         */
        val withStamps
            get() = object : DelegateProvider<Any?, TimeStamped<Input>> {
                override fun provideDelegate(thisRef: Any?, prop: KProperty<*>) = object : ReadOnlyProperty<Any?, TimeStamped<Input>> {
                    override fun getValue(thisRef: Any?, property: KProperty<*>) = getValue(forSensor)
                }.also { startUpdates(forSensor) }
            }
    }
}

/**
 * `Sensor` domain-specific language entry point
 *
 * Helps manage concurrent sensor use across multiple subsystems
 *
 * @receiver subsystem this sensor belongs to
 * @param Input type of sensor data being read
 * @param read function to read new sensor data from the hardware object
 * @return new `Sensor` instance for the given read function
 */
fun <Input> RobotHardware<*>.sensor(read: (Time) -> TimeStamped<Input>) = Sensor(read)

/**
 * `Sensor` domain-specific language entry point
 *
 * Helps manage concurrent sensor use across multiple subsystems
 *
 * @receiver subsystem this sensor belongs to
 * @param Hardw type of hardware object providing sensor data
 * @param Input type of sensor data being read
 * @param hardw hardware object providing sensor data
 * @param read function to read new sensor data from the hardware object
 * @return new `Sensor` instance for the given read function and hardware object
 */
fun <Hardw, Input> RobotHardware<*>.sensor(hardw: Hardw, read: Hardw.(Time) -> TimeStamped<Input>) = Sensor { read(hardw, it) }

/**
 * Graph new sensor data whenever it is read
 *
 * @param QInput type of sensor data being graphed
 * @param graph instance to write sensor data to
 * @return new `Sensor` instance with the given grapher
 */
fun <QInput : Quan<QInput>> Sensor<QInput>.with(graph: Grapher<QInput>) =
        Sensor { t -> read(t).also { graph(it.x, it.y) } }

/**
 * Graph new sensor data whenever it is read
 *
 * @param Input type of sensor data being read
 * @param QInput type of data being graphed
 * @param graph instance to write sensor data to
 * @param structure function to convert sensor data, `Input`, to graph data `QInput`
 * @return new `Sensor` instance with the given grapher
 */
fun <Input, QInput : Quan<QInput>> Sensor<Input>.with(graph: Grapher<QInput>, structure: (Input) -> QInput) =
        Sensor { t -> read(t).also { graph(it.x, structure(it.y)) } }