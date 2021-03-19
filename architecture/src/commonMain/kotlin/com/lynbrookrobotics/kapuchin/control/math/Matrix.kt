package com.lynbrookrobotics.kapuchin.control.math

import info.kunalsheth.units.generated.*

class Matrix(num_rows: Int, num_cols: Int, matrix: Array<Array<T?>>){
    private var storage_matrix: Array<Array<T?>> = matrix
    private val rows = num_rows
    private val cols = num_cols
    init {
        storage_matrix = Array<Array<T?>>(rows){ arrayOfNulls<T?>(cols) }
    }

    infix fun `+` (other: Matrix): Matrix? {
        if(rows != other.rows || cols != other.cols) return null
        val returnMatrix = Matrix(rows, cols, storage_matrix)

        for(i in 0..storage_matrix.size){
            for(j in 0..storage_matrix[i].size){
                val otherVal = other.storage_matrix[i][j];
                if(otherVal == null) returnMatrix.storage_matrix[i][j] = storage_matrix[i][j]
                else returnMatrix.storage_matrix[i][j] = storage_matrix[i][j]?.plus(otherVal)
            }
        }
        return returnMatrix
    }

    infix fun `-` (other: Matrix): Matrix? {
        if(rows != other.rows || cols != other.cols) return null
        val returnMatrix = Matrix(rows, cols, storage_matrix)

        for(i in 0..storage_matrix.size){
            for(j in 0..storage_matrix[i].size){
                val otherVal = other.storage_matrix[i][j];
                if(otherVal == null) returnMatrix.storage_matrix[i][j] = storage_matrix[i][j]
                else returnMatrix.storage_matrix[i][j] = storage_matrix[i][j]?.minus(otherVal)
            }
        }
        return returnMatrix
    }

//    infix fun `*` (other: Matrix): Matrix? {
//        if(cols != other.rows) return null
//
//        val returnMatrix = Matrix(rows, other.cols, Array<Array<T?>>(cols){ arrayOfNulls<T?>(other.rows) })
//
//        for(i in 0 until rows){
//            for(j in 0 until other.cols){
//                for(k in 0..other.rows){
//                    val otherVal = other.storage_matrix[i][j];
//                    if(otherVal == null) returnMatrix.storage_matrix[i][j] = storage_matrix[i][j]
//                    else returnMatrix.storage_matrix[i][j] = storage_matrix[i][j]?.times(otherVal)
//                }
//            }
//        }
//
//        return returnMatrix
//    }

}

