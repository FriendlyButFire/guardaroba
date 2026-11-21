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
    /**
     * Il Capo da salvare, oppure null se manca un campo obbligatorio. Modificando, copy() di [iniziale] tiene id, creatoIl
     * e ogni campo che la scheda non mostra; senza [iniziale] è un Capo nuovo.
     */
    fun capo(iniziale: Capo?): Capo? {
        val c = categoria ?: return null
        val t = tipo ?: return null
        val p = colorePrincipale ?: return null
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
