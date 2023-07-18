package com.senamerdin.productdefectrecordingsystem

class Product(
    val barcode: String? = null,
    val type: String? = null,
    val model: String? = null,
    val controller: String? = null,
    val fixer: String? = null,
    val serieNumber: String? = null,
    val rowNumber: String? = null,
    val color: String? = null,
    val defectCode: String? = null
) {
    // İhtiyaç duyulursa sınıfa ek işlemler veya yöntemler eklenebilir
}