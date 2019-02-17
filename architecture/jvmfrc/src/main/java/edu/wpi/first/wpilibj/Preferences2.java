/*----------------------------------------------------------------------------*/
/* Copyright (c) 2008-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj;

import edu.wpi.first.hal.FRCNetComm.tResourceType;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.networktables.*;

import java.util.Vector;

import static java.util.Objects.requireNonNull;

/**
 * Copy and paste of Preferences but changes the NetworkTable to public
 *
 * @author Andy
 * @see edu.wpi.first.wpilibj.Preferences
 */
public final class Preferences2 {
    /**
     * The Preferences2 table name.
     */
    private static final String TABLE_NAME = "Preferences";
    /**
     * The singleton instance.
     */
    private static Preferences2 instance;
    /**
     * The network table. (now public)
     */
    public final NetworkTable table;

    /**
     * Returns the preferences instance.
     *
     * @return the preferences instance
     */
    public static synchronized Preferences2 getInstance() {
        if (instance == null) {
            instance = new Preferences2();
        }
        return instance;
    }

    /**
     * Creates a preference class.
     */
    private Preferences2() {
        table = NetworkTableInstance.getDefault().getTable(TABLE_NAME);
        table.getEntry(".type").setString("RobotPreferences2");
        // Listener to set all Preferences2 values to persistent
        // (for backwards compatibility with old dashboards).
        table.addEntryListener(
                (table, key, entry, value, flags) -> entry.setPersistent(),
                EntryListenerFlags.kImmediate | EntryListenerFlags.kNew);
        HAL.report(tResourceType.kResourceType_Preferences, 0);
    }

    /**
     * Gets the vector of keys.
     *
     * @return a vector of the keys
     */
    @SuppressWarnings({"PMD.LooseCoupling", "PMD.UseArrayListInsteadOfVector"})
    public Vector<String> getKeys() {
        return new Vector<>(table.getKeys());
    }

    /**
     * Puts the given string into the preferences table.
     *
     * @param key   the key
     * @param value the value
     * @throws NullPointerException if value is null
     */
    public void putString(String key, String value) {
        requireNonNull(value, "Provided value was null");

        NetworkTableEntry entry = table.getEntry(key);
        entry.setString(value);
        entry.setPersistent();
    }

    /**
     * Puts the given int into the preferences table.
     *
     * @param key   the key
     * @param value the value
     */
    public void putInt(String key, int value) {
        NetworkTableEntry entry = table.getEntry(key);
        entry.setDouble(value);
        entry.setPersistent();
    }

    /**
     * Puts the given double into the preferences table.
     *
     * @param key   the key
     * @param value the value
     */
    public void putDouble(String key, double value) {
        NetworkTableEntry entry = table.getEntry(key);
        entry.setDouble(value);
        entry.setPersistent();
    }

    /**
     * Puts the given float into the preferences table.
     *
     * @param key   the key
     * @param value the value
     */
    public void putFloat(String key, float value) {
        NetworkTableEntry entry = table.getEntry(key);
        entry.setDouble(value);
        entry.setPersistent();
    }

    /**
     * Puts the given boolean into the preferences table.
     *
     * @param key   the key
     * @param value the value
     */
    public void putBoolean(String key, boolean value) {
        NetworkTableEntry entry = table.getEntry(key);
        entry.setBoolean(value);
        entry.setPersistent();
    }

    /**
     * Puts the given long into the preferences table.
     *
     * @param key   the key
     * @param value the value
     */
    public void putLong(String key, long value) {
        NetworkTableEntry entry = table.getEntry(key);
        entry.setDouble(value);
        entry.setPersistent();
    }

    /**
     * Returns whether or not there is a key with the given name.
     *
     * @param key the key
     * @return if there is a value at the given key
     */
    public boolean containsKey(String key) {
        return table.containsKey(key);
    }

    /**
     * Remove a preference.
     *
     * @param key the key
     */
    public void remove(String key) {
        table.delete(key);
    }

    /**
     * Remove all preferences.
     */
    public void removeAll() {
        for (String key : table.getKeys()) {
            if (!".type".equals(key)) {
                remove(key);
            }
        }
    }

    /**
     * Returns the string at the given key. If this table does not have a value for that position,
     * then the given backup value will be returned.
     *
     * @param key    the key
     * @param backup the value to return if none exists in the table
     * @return either the value in the table, or the backup
     */
    public String getString(String key, String backup) {
        return table.getEntry(key).getString(backup);
    }

    /**
     * Returns the int at the given key. If this table does not have a value for that position, then
     * the given backup value will be returned.
     *
     * @param key    the key
     * @param backup the value to return if none exists in the table
     * @return either the value in the table, or the backup
     */
    public int getInt(String key, int backup) {
        return (int) table.getEntry(key).getDouble(backup);
    }

    /**
     * Returns the double at the given key. If this table does not have a value for that position,
     * then the given backup value will be returned.
     *
     * @param key    the key
     * @param backup the value to return if none exists in the table
     * @return either the value in the table, or the backup
     */
    public double getDouble(String key, double backup) {
        return table.getEntry(key).getDouble(backup);
    }

    /**
     * Returns the boolean at the given key. If this table does not have a value for that position,
     * then the given backup value will be returned.
     *
     * @param key    the key
     * @param backup the value to return if none exists in the table
     * @return either the value in the table, or the backup
     */
    public boolean getBoolean(String key, boolean backup) {
        return table.getEntry(key).getBoolean(backup);
    }

    /**
     * Returns the float at the given key. If this table does not have a value for that position, then
     * the given backup value will be returned.
     *
     * @param key    the key
     * @param backup the value to return if none exists in the table
     * @return either the value in the table, or the backup
     */
    public float getFloat(String key, float backup) {
        return (float) table.getEntry(key).getDouble(backup);
    }

    /**
     * Returns the long at the given key. If this table does not have a value for that position, then
     * the given backup value will be returned.
     *
     * @param key    the key
     * @param backup the value to return if none exists in the table
     * @return either the value in the table, or the backup
     */
    public long getLong(String key, long backup) {
        return (long) table.getEntry(key).getDouble(backup);
    }
}
