package com.lynbrookrobotics.kapuchin.delegates.preferences

import com.lynbrookrobotics.kapuchin.Quan

class UomPreference<Value>(
        conversion: (Double) -> Value, uomName: String,
        fallback: Double, get: (String, Double) -> Double
) : Preference<Value>(
        fallback = conversion(fallback),
        get = { name, _ -> conversion(get(name, fallback)) },
        nameSuffix = " ($uomName)"
)
        where Value : Quan<Value>
