package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch
import java.io.Closeable
import java.io.File
import java.io.Flushable

actual class Grapher<Q : Quan<Q>> internal actual constructor(
    parent: Named,
    of: String,
    private val withUnits: UomConverter<Q>
) :
    Named by Named("$of (${withUnits.unitName})", parent),
    Flushable, Closeable,
        (Time, Q) -> Unit {

    private var running = scope.launch { }
    private val safeName = name.replace("""[^\w\d]""".toRegex(), "_")
    private val printer = File("/tmp/$safeName.csv")
        .printWriter(Charsets.US_ASCII).also { it.println("seconds,${withUnits.unitName}") }

    actual override fun invoke(x: Time, y: Q) {
        if (running.isCompleted) running = scope.launch {
            if (graphToDashboard)
                SmartDashboard.putNumber(name, withUnits(y))
            if (graphToFile) {
                printer.println("${x.Second},${withUnits(y)}")
                printer.flush()
            }
        }
    }

    actual override fun flush() = printer.flush()
    actual override fun close() = printer.close()

    companion object : Named by Named("graphers") {
        val graphToFile by pref(false)
        val graphToDashboard by pref(true)
    }
}
