package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.control.data.*


sealed class Target
class InnerGoal(innerPos: Position){
    val possible = "ks"
}
class OuterGoal(outerPos: Position){

}