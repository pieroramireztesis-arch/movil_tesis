// app/src/main/java/com/example/aplicacion_tesis/ui/home/tabs/TemaDominio.kt
package com.example.aplicacion_tesis.ui.home.tabs

data class TemaDominio(
    val idTema:              Int,
    val nombre:              String,
    val nivel:               String,
    val totalMateriales:     Int,
    val materialesVistos:    Int,
    val materialesBasico:    Int = 0,
    val materialesIntermedio:Int = 0,
    val materialesAvanzado:  Int = 0
) {
    val porcentajeProgreso: Int
        get() = if (totalMateriales > 0) materialesVistos * 100 / totalMateriales else 0
}
