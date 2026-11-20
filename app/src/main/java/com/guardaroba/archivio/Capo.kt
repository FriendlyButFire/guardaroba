package com.guardaroba.archivio

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Motivo
import com.guardaroba.motore.Stagione
import kotlinx.coroutines.flow.Flow

/** Un Capo del Guardaroba. I Colori sono salvati come id della Palette, mai come valori. */
@Entity(tableName = "capo")
data class Capo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoria: Categoria,
    val tipo: String,
    val colorePrincipale: String,
    val coloreSecondario: String? = null,
    @ColumnInfo(defaultValue = "TINTA_UNITA") val motivo: Motivo = Motivo.TINTA_UNITA,
    @ColumnInfo(defaultValue = "TUTTO_L_ANNO") val stagione: Stagione = Stagione.TUTTO_L_ANNO,
    val taglia: String? = null,
    /** Il nome del file in [Foto], oppure null: la griglia mostra il riquadro del Colore principale. */
    val percorsoFoto: String? = null,
    val creatoIl: Long = System.currentTimeMillis(),
)

@Dao
interface CapoDao {
    @Query("SELECT * FROM capo ORDER BY creatoIl")
    fun tutti(): Flow<List<Capo>>

    /** Inserisce un Capo nuovo (id 0) e ne ritorna l'id, oppure aggiorna quello con lo stesso id. */
    @Upsert
    suspend fun salva(capo: Capo): Long

    /** Come [salva], poi cancella la foto che il Capo non usa più perché sostituita o rimossa. */
    suspend fun salva(capo: Capo, foto: Foto): Long {
        val precedente = percorsoFoto(capo.id)
        return salva(capo).also { if (precedente != capo.percorsoFoto) foto.elimina(precedente) }
    }

    @Query("SELECT percorsoFoto FROM capo WHERE id = :id")
    suspend fun percorsoFoto(id: Long): String?

    /** Quanti Preferiti contengono il Capo: tanti ne sparirebbero eliminandolo. */
    @Query("SELECT COUNT(*) FROM preferito_capo WHERE capoId = :id")
    suspend fun quantiPreferiti(id: Long): Int

    /** Elimina il Capo, i Preferiti che lo contengono e poi, a eliminazione riuscita, il file della sua foto. */
    suspend fun elimina(id: Long, foto: Foto) {
        foto.elimina(eliminaConPreferiti(id))
    }

    /** Elimina il Capo e i Preferiti che lo contengono; ritorna la foto che il Capo usava. */
    @Transaction
    suspend fun eliminaConPreferiti(id: Long): String? = percorsoFoto(id).also {
        eliminaPreferitiCon(id)
        eliminaCapo(id)
    }

    // La cascata su preferito_capo.capoId toglierebbe solo la riga: il Preferito va eliminato qui, prima del Capo.
    @Query("DELETE FROM preferito WHERE id IN (SELECT preferitoId FROM preferito_capo WHERE capoId = :id)")
    suspend fun eliminaPreferitiCon(id: Long)

    @Query("DELETE FROM capo WHERE id = :id")
    suspend fun eliminaCapo(id: Long)
}
