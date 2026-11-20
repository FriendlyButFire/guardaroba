package com.guardaroba.archivio

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
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
    val creatoIl: Long = System.currentTimeMillis(),
)

@Dao
interface CapoDao {
    @Query("SELECT * FROM capo ORDER BY creatoIl")
    fun tutti(): Flow<List<Capo>>

    @Insert
    suspend fun inserisci(capo: Capo): Long
}
