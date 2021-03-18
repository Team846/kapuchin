package com.lynbrookrobotics.kapuchin.control.math

import info.kunalsheth.units.generated.*

class Matrix(num_rows: Int, num_cols: Int, matrix: Array<Array<T?>>){
    var storage_matrix: Array<Array<T?>> = matrix
    val num_rows = num_rows
    val num_cols = num_cols
    init {
        storage_matrix = Array<Array<T?>>(num_rows){ arrayOfNulls<T?>(num_cols) }
    }

//    inline infix fun `+` (other: Matrix): Matrix? {
//        if (num_cols != other.num_cols || num_rows != other.num_rows){
//            return null
//        }
//        else
//        {
//            val ret = Matrix(num_rows, num_cols, storage_matrix)
//            for (i in other.storage_matrix){
//                for(j in i){
//                    ret.storage_matrix[storage_matrix.indexOf(i)][i.indexOf(j)] =
//                        storage_matrix[storage_matrix.indexOf(i)][storage_matrix[storage_matrix.indexOf(i)]?.indexOf(j)]
//                    + other.storage_matrix[storage_matrix.indexOf(i)][storage_matrix[storage_matrix.indexOf(i)].indexOf(j)]
//                }
//            }
//        }
//    }
}