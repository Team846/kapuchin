package com.lynbrookrobotics.kapuchin.preferences

import edu.wpi.first.networktables.EntryListenerFlags

fun registerCallback(callback: () -> Unit) {
    Preferences2.getInstance().table.addEntryListener({ _, _, _, _, _ ->
        callback()
    }, EntryListenerFlags.kNew or EntryListenerFlags.kUpdate)
}