package com.guardaroba.archivio

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.guardaroba.motore.Categoria
import kotlinx.coroutines.flow.Flow

/** Un Capo del Guardaroba. I Colori sono salvati come id della Palette, mai come valori. */
@Entity(tableName = "capo")
data class Capo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoria: Categoria,
    val tipo: String,
    val colorePrincipale: String,
    val creatoIl: Long = System.currentTimeMillis(),
)

@Dao
interface CapoDao {
    @Query("SELECT * FROM capo ORDER BY creatoIl")
    fun tutti(): Flow<List<Capo>>

    @Insert
    suspend fun inserisci(capo: Capo): Long
}
