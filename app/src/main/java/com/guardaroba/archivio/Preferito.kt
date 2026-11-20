package com.guardaroba.archivio

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/** Un Outfit salvato dall'utente, con la Spiegazione com'era al salvataggio. */
@Entity(tableName = "preferito")
data class Preferito(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String? = null,
    val spiegazione: String,
    val creatoIl: Long = System.currentTimeMillis(),
)

/** Un Capo di un Preferito. Le foreign key in cascata tolgono la riga quando sparisce il Preferito o il Capo. */
// shortcut: la cascata su capoId toglie solo questa riga, il Preferito resta con un Capo in meno;
// il ticket 17 deve eliminare nella stessa transazione i Preferiti che contengono il Capo.
@Entity(
    tableName = "preferito_capo",
    primaryKeys = ["preferitoId", "capoId"],
    foreignKeys = [
        ForeignKey(entity = Preferito::class, parentColumns = ["id"], childColumns = ["preferitoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Capo::class, parentColumns = ["id"], childColumns = ["capoId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("capoId")],
)
data class PreferitoCapo(val preferitoId: Long, val capoId: Long)

data class PreferitoConCapi(
    @Embedded val preferito: Preferito,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(PreferitoCapo::class, parentColumn = "preferitoId", entityColumn = "capoId"),
    )
    val capi: List<Capo>,
)

@Dao
interface PreferitoDao {
    @Transaction
    @Query("SELECT * FROM preferito ORDER BY creatoIl DESC")
    fun tutti(): Flow<List<PreferitoConCapi>>

    /** Salva l'Outfit tra i Preferiti; se c'è già (stesso insieme di Capi) restituisce l'id esistente. */
    @Transaction
    suspend fun salva(spiegazione: String, idCapi: Set<Long>): Long =
        conICapi(idCapi, idCapi.size) ?: inserisci(Preferito(spiegazione = spiegazione)).also { id ->
            inserisci(idCapi.map { PreferitoCapo(id, it) })
        }

    /** Il Preferito fatto esattamente di questi Capi, né uno di più né uno di meno. */
    @Query(
        "SELECT preferitoId FROM preferito_capo GROUP BY preferitoId " +
            "HAVING COUNT(*) = :quanti AND SUM(capoId IN (:idCapi)) = :quanti LIMIT 1",
    )
    suspend fun conICapi(idCapi: Set<Long>, quanti: Int): Long?

    @Insert
    suspend fun inserisci(preferito: Preferito): Long

    @Insert
    suspend fun inserisci(righe: List<PreferitoCapo>)
}
