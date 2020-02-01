package com.lynbrookrobotics.kapuchin.subsystems.limelight

class LimelightOutput (pipe: Pipeline?, panX: Number, panY: Number) {
     val pipe = pipe
    val panX = panX
    val panY = panY
}

enum class Pipeline(val number: Int) {
    ZoomOut(0), ZoomIn(1), DriverStream(2)
}