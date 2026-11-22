package com.guardaroba.archivio

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Motivo
import com.guardaroba.motore.Stagione
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrazioneTest {
    @Test
    fun `un Capo della versione 1 diventa tinta unita, tutto l'anno, senza Colore secondario, Taglia né foto`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Il database v1 come lo lasciava il ticket 02 (createSql e identityHash da app/schemas/.../1.json).
        // shortcut: copiato a mano perché MigrationTestHelper legge gli schemi solo dagli assets, che Robolectric
        // non vede nei test; passare a MigrationTestHelper se si aggiungono test strumentati.
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(GuardarobaDatabase.NOME).apply { parentFile!!.mkdirs() }, null).run {
            execSQL("CREATE TABLE capo (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `categoria` TEXT NOT NULL, `tipo` TEXT NOT NULL, `colorePrincipale` TEXT NOT NULL, `creatoIl` INTEGER NOT NULL)")
            execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
            execSQL("INSERT INTO room_master_table (id,identity_hash) VALUES(42, '83f8c88c7fbd568c03365f0504c6714f')")
            execSQL("INSERT INTO capo (categoria, tipo, colorePrincipale, creatoIl) VALUES ('SOPRA', 'camicia', 'blu_navy', 1)")
            version = 1
            close()
        }

        // Room esegue in catena le AutoMigration 1→2→3→4 e verifica lo schema risultante contro l'entity.
        val db = GuardarobaDatabase.apri(context)
        val capi = db.capoDao().tutti().first()
        db.close()

        val atteso = Capo(
            id = 1, categoria = Categoria.SOPRA, tipo = "camicia", colorePrincipale = "blu_navy",
            coloreSecondario = null, motivo = Motivo.TINTA_UNITA, stagione = Stagione.TUTTO_L_ANNO, taglia = null, percorsoFoto = null, creatoIl = 1,
        )
        assertEquals(listOf(atteso), capi)
    }
}
