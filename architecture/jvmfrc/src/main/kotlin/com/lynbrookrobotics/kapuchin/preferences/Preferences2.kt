package com.lynbrookrobotics.kapuchin.preferences

import edu.wpi.first.hal.FRCNetComm.tResourceType
import edu.wpi.first.hal.HAL
import edu.wpi.first.networktables.EntryListenerFlags
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import java.util.*

/**
 * Copy and paste of edu.wpi.first.wpilibj.Preferences but adds listener for change in NetworkTable
 *
 * @author Andy
 *
 * @see com.lynbrookrobotics.kapuchin.preferences.Preference
 * @see edu.wpi.first.wpilibj.Preferences
 */

class Preferences2 private constructor() {

    val table: NetworkTable = NetworkTableInstance.getDefault().getTable("Preferences");
    private val callbacks = ArrayList<() -> Unit>()

    companion object {
        private var instance: Preferences2? = null

        fun getInstance(): Preferences2 {
            if (instance == null) {
                instance = Preferences2()
            }
            assert(instance == null)
            return instance!!
        }
    }

    init {
        table.getEntry(".type").setString("RobotPreferences")
        table.addEntryListener({ _, _, entry, _, _ ->

            entry.setPersistent()
            for (callback in callbacks) {
                callback()
            }

        }, EntryListenerFlags.kImmediate or EntryListenerFlags.kNew or EntryListenerFlags.kUpdate)
        HAL.report(tResourceType.kResourceType_Preferences, 0)
    }

    fun registerCallback(callback: () -> Unit) {
        callbacks.add(callback)
    }

    val keys = Vector(table.keys)

    fun containsKey(key: String): Boolean {
        return table.containsKey(key)
    }

    fun remove(key: String) {
        table.delete(key)
    }

    fun removeAll() {
        for (key in table.keys) {
            if (".type" != key) {
                remove(key)
            }
        }
    }

    fun putString(key: String, value: String?) {
        if (value == null) {
            throw NullPointerException("Provided value was null")
        }

        val entry = table.getEntry(key)
        entry.setString(value)
        entry.setPersistent()
    }

    fun putInt(key: String, value: Int) {
        val entry = table.getEntry(key)
        entry.setDouble(value.toDouble())
        entry.setPersistent()
    }

    fun putDouble(key: String, value: Double) {
        val entry = table.getEntry(key)
        entry.setDouble(value)
        entry.setPersistent()
    }

    fun putFloat(key: String, value: Float) {
        val entry = table.getEntry(key)
        entry.setDouble(value.toDouble())
        entry.setPersistent()
    }

    fun putBoolean(key: String, value: Boolean) {
        val entry = table.getEntry(key)
        entry.setBoolean(value)
        entry.setPersistent()
    }

    fun putLong(key: String, value: Long) {
        val entry = table.getEntry(key)
        entry.setDouble(value.toDouble())
        entry.setPersistent()
    }

    fun getString(key: String, backup: String): String {
        return table.getEntry(key).getString(backup)
    }

    fun getInt(key: String, backup: Int): Int {
        return table.getEntry(key).getDouble(backup.toDouble()).toInt()
    }

    fun getDouble(key: String, backup: Double): Double {
        return table.getEntry(key).getDouble(backup)
    }

    fun getBoolean(key: String, backup: Boolean): Boolean {
        return table.getEntry(key).getBoolean(backup)
    }

    fun getFloat(key: String, backup: Float): Float {
        return table.getEntry(key).getDouble(backup.toDouble()).toFloat()
    }

    fun getLong(key: String, backup: Long): Long {
        return table.getEntry(key).getDouble(backup.toDouble()).toLong()
    }


}


