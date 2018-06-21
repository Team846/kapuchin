package com.lynbrookrobotics.kapuchin.delegates.preferences

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.subsystems.Named

class UomPreference<Value>(
        conversion: (Double) -> Value, uomName: String,
        thisRef: Named, fallback: Double, get: (String, Double) -> Double
) : Preference<Value>(
        thisRef = thisRef,
        fallback = conversion(fallback),
        get = { name, _ -> conversion(get(name, fallback)) },
        nameSuffix = " ($uomName)"
)
        where Value : Quan<Value>
