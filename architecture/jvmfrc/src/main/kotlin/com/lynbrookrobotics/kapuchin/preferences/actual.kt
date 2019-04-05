package com.lynbrookrobotics.kapuchin.preferences

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.networktables.EntryListenerFlags
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.wpilibj.Preferences2
import info.kunalsheth.units.generated.*

private val impl = Preferences2.getInstance()

actual fun Named.pref(fallback: Boolean) = Preference(this, fallback, impl::putBoolean, impl::getBoolean, impl::containsKey, ::registerCallback)
actual fun Named.pref(fallback: Double) = Preference(this, fallback, impl::putDouble, impl::getDouble, impl::containsKey, ::registerCallback)
actual fun Named.pref(fallback: Float) = Preference(this, fallback, impl::putFloat, impl::getFloat, impl::containsKey, ::registerCallback)
actual fun Named.pref(fallback: Int) = Preference(this, fallback, impl::putInt, impl::getInt, impl::containsKey, ::registerCallback)
actual fun Named.pref(fallback: Long) = Preference(this, fallback, impl::putLong, impl::getLong, impl::containsKey, ::registerCallback)
actual fun Named.pref(fallback: String) = Preference(this, fallback, impl::putString, impl::getString, impl::containsKey, ::registerCallback)

actual fun <Q : Quan<Q>> Named.pref(fallback: Number, withUnits: UomConverter<Q>) = Preference(
        this, withUnits(fallback.toDouble()),
        { name, value -> impl.putDouble(name, withUnits(value)) },
        { name, value -> withUnits(impl.getDouble(name, withUnits(value))) },
        impl::containsKey,
        ::registerCallback,
        " (${withUnits.unitName})"
)

fun <Q : Quan<Q>> Named.pref(
        defaultOpenloopRamp: Time = 0.Second,
        defaultClosedloopRamp: Time = 0.Second,
        defaultPeakOutput: DutyCycle = 100.Percent,
        defaultNominalOutput: DutyCycle = 0.Percent,
        defaultVoltageCompSaturation: V = 12.Volt,
        defaultContinuousCurrentLimit: I = 25.Ampere,
        defaultPeakCurrentLimit: I = 40.Ampere,
        defaultPeakCurrentDuration: Time = 1.Second
) = pref {

    val openloopRamp by pref(defaultOpenloopRamp.Second, Second)
    val closedloopRamp by pref(defaultClosedloopRamp.Second, Second)

    val peakOutput by pref(defaultPeakOutput.Percent, Percent)
    val nominalOutput by pref(defaultNominalOutput.Percent, Percent)

    val voltageCompSaturation by pref(defaultVoltageCompSaturation.Volt, Volt)

    val continuousCurrentLimit by pref(defaultContinuousCurrentLimit.Ampere, Ampere)
    val peakCurrentLimit by pref(defaultPeakCurrentLimit.Ampere, Ampere)
    val peakCurrentDuration by pref(defaultPeakCurrentDuration.Second, Second)

    ({
        OffloadedEscConfiguration(
                openloopRamp = openloopRamp,
                closedloopRamp = closedloopRamp,

                peakOutputForward = peakOutput,
                nominalOutputForward = nominalOutput,
                nominalOutputReverse = -nominalOutput,
                peakOutputReverse = -peakOutput,

                voltageCompSaturation = voltageCompSaturation,
                continuousCurrentLimit = continuousCurrentLimit,
                peakCurrentLimit = peakCurrentLimit,
                peakCurrentDuration = peakCurrentDuration
        )
    })
}


/**
 * Adds an EntryListener to the NetworkTable in Preferences2
 *
 * @author Andy
 * @param key the name of the key to register the callback with
 * @param callback the callback function to call
 * @see edu.wpi.first.wpilibj.Preferences2
 */
private fun registerCallback(key: String, callback: () -> Unit) {
    blockingMutex(keys) {
        keys += NetworkTable.basenameKey(key)
        Preferences2.getInstance().table.addEntryListener(key, { _, _, _, _, _ ->
            callback()
        }, EntryListenerFlags.kNew or EntryListenerFlags.kUpdate)
    }
}

/**
 * Deletes all unused NetworkTable entries
 *
 * @author Andy
 * @see Preferences2
 * @see NetworkTable
 */
private val keys = mutableSetOf<String>()

fun trim(table: NetworkTable = impl.table) {
    //Gets rid of all unused keys in the current subTable
    table.keys.forEach {
        if (it !in keys) {
            println("Trimming $it")
            table.getEntry(it).clearPersistent()
            table.getEntry(it).delete()
        }
    }
    //Recurse through all subTables of the current subTable
    table.subTables.forEach { trim(table.getSubTable(it)) }
}

