package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Hardware initialization domain-specific language
 *
 * Helps avoid logging boilerplate when initializing hardware
 *
 * @author Kunal
 * @see SubsystemHardware
 * @see Sensor
 * @see DelegateProvider
 *
 * @param Hardw type of hardware object being initialized
 */
class HardwareInit<Hardw> internal constructor(
        private val parent: SubsystemHardware<*, *>,
        private val initialize: Named.() -> Hardw,
        private val configure: Named.(Hardw) -> Unit = {},
        private val validate: Named.(Hardw) -> Boolean = { true },
        private val alternative: HardwareInit<Hardw>? = null,
        private val nameSuffix: String = ""
) : DelegateProvider<Any?, Hardw> {

    override fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, Hardw> = Named(prop.name + nameSuffix, parent).run {
        try {
            log(Debug) { "Initializing" }
            val value = initialize()
                    .also { configure(it) }
                    .also {
                        if (!validate(it) && crashOnFailure)
                            error("Initialized hardware is invalid.")
                    }

            object : ReadOnlyProperty<Any?, Hardw> {
                override fun getValue(thisRef: Any?, property: KProperty<*>) = value!!
            }

        } catch (t: Throwable) {
            log(Error, t) { "Error during creation.\nMessage: ${t.message}\nCause: ${t.cause}" }
            alternative?.provideDelegate(thisRef, prop) ?: throw t
        }
    }

    /**
     * Safely configure the hardware object
     *
     * @param f function to configure the hardware object
     * @return new `HardwareInit` delegate with the given configuration
     */
    fun configure(f: Named.(Hardw) -> Unit) = HardwareInit(
            parent, initialize, { configure(it); f(it) }, validate, alternative, nameSuffix
    )

    /**
     * Verify that the configured hardware object meets a certain condition
     *
     * @param that description of what is being verified
     * @param f function to validate the hardware object after configuration
     * @return new `HardwareInit` delegate with the given verification
     */
    fun verify(that: String, f: Named.(Hardw) -> Boolean) = HardwareInit(
            parent, initialize, configure,
            { validate(it) && f(it).also { if (!it) log(Error) { that } } },
            alternative, nameSuffix
    )

    /**
     * Add some alternative hardware to use in-case this hardware fails to initialize
     *
     * @param useThis alternative hardware to initialize
     * @return new `HardwareInit` delegate with the given alternative
     */
    fun otherwise(useThis: HardwareInit<Hardw>): HardwareInit<Hardw> = HardwareInit(
            parent, initialize, configure, validate,
            alternative?.otherwise(useThis) ?: useThis,
            nameSuffix
    )

    companion object : Named by Named("Hardware Initialization") {
        val crashOnFailure by pref(true)
    }
}

/**
 * `HardwareInit` domain-specific language entry point
 *
 * Helps avoid logging boilerplate when initializing hardware
 *
 * @receiver subsystem this hardware belongs to
 * @param Hardw type of hardware object being initialized
 * @param nameSuffix logging name suffix
 * @param initialize function to instantiate hardware object
 * @return new `HardwareInit` delegate for the given hardware object
 */
fun <Hardw> SubsystemHardware<*, *>.hardw(nameSuffix: String = "", initialize: Named.() -> Hardw) = HardwareInit(this, initialize, nameSuffix = nameSuffix)