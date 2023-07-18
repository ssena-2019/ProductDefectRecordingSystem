package com.senamerdin.productdefectrecordingsystem

import java.io.Serializable

data class Defect(
    val defectCode: String,
    val name: String,
    val tur: String,
    val fixed: String,
    val url: String

) : Serializable {
    // Copy constructor
    constructor(defect: Defect) : this(
        defect.defectCode,
        defect.name,
        defect.tur,
        defect.fixed,
        defect.url
    )
}
