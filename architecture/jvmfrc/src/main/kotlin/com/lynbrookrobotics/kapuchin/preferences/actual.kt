package com.lynbrookrobotics.kapuchin.preferences

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.timing.blockingMutex
import edu.wpi.first.networktables.EntryListenerFlags
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.wpilibj.Preferences2
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.UomConverter

private val impl = Preferences2.getInstance()

actual fun Named.pref(fallback: Boolean) = Preference(this, fallback, impl::putBoolean, impl::getBoolean, ::registerCallback)
actual fun Named.pref(fallback: Double) = Preference(this, fallback, impl::putDouble, impl::getDouble, ::registerCallback)
actual fun Named.pref(fallback: Float) = Preference(this, fallback, impl::putFloat, impl::getFloat, ::registerCallback)
actual fun Named.pref(fallback: Int) = Preference(this, fallback, impl::putInt, impl::getInt, ::registerCallback)
actual fun Named.pref(fallback: Long) = Preference(this, fallback, impl::putLong, impl::getLong, ::registerCallback)
actual fun Named.pref(fallback: String) = Preference(this, fallback, impl::putString, impl::getString, ::registerCallback)

actual fun <Q : Quan<Q>> Named.pref(fallback: Number, withUnits: UomConverter<Q>) = Preference(
        this, withUnits(fallback),
        { name, value -> impl.putDouble(name, withUnits(value)) },
        { name, value -> withUnits(impl.getDouble(name, withUnits(value))) },
        ::registerCallback,
        " (${withUnits.unitName})"
)


/**
 * Adds an EntryListener to the NetworkTable in Preferences2
 *
 * @author Andy
 * @param key the name of the key to register the callback with
 * @param callback the callback function to call
 * @see edu.wpi.first.wpilibj.Preferences2
 */
private fun registerCallback(key: String, callback: () -> Unit) {
    blockingMutex(key) {
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
            println("Deleting entry: $it")
            table.getEntry(it).clearPersistent()
            table.getEntry(it).delete()
        }
    }
    //Recurse through all subTables of the current subTable
    table.subTables.forEach { trim(table.getSubTable(it)) }

}

