package com.guardaroba.ui

import com.guardaroba.archivio.Capo
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.PALETTE
import com.guardaroba.motore.Stagione
import com.guardaroba.motore.ammette

/** I filtri del Guardaroba, combinati in AND; null = filtro spento. [colore] è un id della Palette. */
data class Filtro(val categoria: Categoria? = null, val colore: String? = null, val stagione: Stagione? = null) {
    val attivo get() = this != Filtro()

    fun ammette(capo: Capo) =
        (categoria == null || capo.categoria == categoria) &&
            (colore == null || capo.colorePrincipale == colore || capo.coloreSecondario == colore) &&
            (stagione == null || stagione.ammette(capo.stagione))

    /**
     * Le Categorie del pannello Filtri: solo quelle dei [capi], più quella già scelta, così si può togliere anche se il suo
     * ultimo Capo non c'è più. Lo stesso vale per [colori].
     */
    fun categorie(capi: List<Capo>) = Categoria.entries.filter { c -> c == categoria || capi.any { it.categoria == c } }

    /** I Colori del pannello Filtri, nell'ordine della Palette: solo i principali, per tenerlo corto; scelto un Colore, [ammette] trova anche i Capi che lo hanno come secondario. */
    fun colori(capi: List<Capo>) = PALETTE.filter { c -> c.id == colore || capi.any { it.colorePrincipale == c.id } }
}
