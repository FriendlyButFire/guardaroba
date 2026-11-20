package com.guardaroba.ui

import com.guardaroba.archivio.Capo
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Stagione
import com.guardaroba.motore.ammette

/** I filtri del Guardaroba, combinati in AND; null = filtro spento. [colore] è un id della Palette. */
data class Filtro(val categoria: Categoria? = null, val colore: String? = null, val stagione: Stagione? = null) {
    val attivo get() = this != Filtro()

    fun ammette(capo: Capo) =
        (categoria == null || capo.categoria == categoria) &&
            (colore == null || capo.colorePrincipale == colore || capo.coloreSecondario == colore) &&
            (stagione == null || stagione.ammette(capo.stagione))
}
