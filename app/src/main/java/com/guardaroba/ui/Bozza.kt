package com.guardaroba.ui

import com.guardaroba.archivio.Capo
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Motivo
import com.guardaroba.motore.Stagione
import java.io.Serializable

/**
 * Quello che la scheda Capo mostra, prima di Salva. Serializable perché rememberSaveable la tenga a una rotazione.
 * Due Bozze uguali = nessuna modifica da scartare.
 */
data class Bozza(
    val categoria: Categoria? = null,
    val tipo: String? = null,
    val colorePrincipale: String? = null,
    val coloreSecondario: String? = null,
    val motivo: Motivo = Motivo.TINTA_UNITA,
    val stagione: Stagione = Stagione.TUTTO_L_ANNO,
    val taglia: String = "",
    val percorsoFoto: String? = null,
) : Serializable {
    /** La regola dei campi obbligatori, unica: i loro nomi come nella scheda, se sono ancora vuoti. */
    val mancano: List<String>
        get() = listOfNotNull(
            "Categoria".takeIf { categoria == null },
            "Tipo".takeIf { tipo == null },
            "Colore principale".takeIf { colorePrincipale == null },
        )

    /**
     * Il Capo da salvare, oppure null se [mancano] non è vuota. Modificando, copy() di [iniziale] tiene id, creatoIl
     * e ogni campo che la scheda non mostra; senza [iniziale] è un Capo nuovo.
     */
    fun capo(iniziale: Capo?): Capo? {
        if (mancano.isNotEmpty()) return null
        val c = categoria!!
        val t = tipo!!
        val p = colorePrincipale!!
        return (iniziale ?: Capo(categoria = c, tipo = t, colorePrincipale = p)).copy(
            categoria = c, tipo = t, colorePrincipale = p, coloreSecondario = coloreSecondario,
            motivo = motivo, stagione = stagione, taglia = taglia.trim().ifEmpty { null }, percorsoFoto = percorsoFoto,
        )
    }
}

/** La Bozza per modificare questo Capo. */
fun Capo.bozza() = Bozza(categoria, tipo, colorePrincipale, coloreSecondario, motivo, stagione, taglia.orEmpty(), percorsoFoto)

/** Duplica: un Capo nuovo uguale a questo, senza la Foto (un file appartiene a un solo Capo, ADR-0013). */
fun Capo.duplicato() = bozza().copy(percorsoFoto = null)
