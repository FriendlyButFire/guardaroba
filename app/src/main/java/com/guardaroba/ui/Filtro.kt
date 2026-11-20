package com.guardaroba.ui

import com.guardaroba.archivio.Capo
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Stagione

/** I filtri del Guardaroba, combinati in AND; null = filtro spento. [colore] è un id della Palette. */
data class Filtro(val categoria: Categoria? = null, val colore: String? = null, val stagione: Stagione? = null) {
    val attivo get() = this != Filtro()

    fun ammette(capo: Capo) =
        (categoria == null || capo.categoria == categoria) &&
            (colore == null || capo.colorePrincipale == colore || capo.coloreSecondario == colore) &&
            // I Capi "tutto l'anno" entrano in qualsiasi filtro Stagione.
            (stagione == null || capo.stagione == stagione || capo.stagione == Stagione.TUTTO_L_ANNO)
}
