package com.guardaroba.archivio

import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * L'export del Guardaroba (ADR-0003, formato in ADR-0007): uno ZIP con [NOME_JSON] (versione del formato, Capi, Preferiti)
 * e una voce `foto/<nome>` per ogni Foto. Nel JSON ogni campo ha il nome della proprietà dell'entità;
 * i Preferiti elencano gli id dei loro Capi in `capi`.
 */
class Esportazione(private val db: GuardarobaDatabase, private val foto: Foto) {

    /** Scrive lo ZIP in [out] e lo chiude. Da chiamare fuori dal main thread. */
    suspend fun esporta(out: OutputStream) {
        // Prima i Capi, poi i Preferiti: un Capo eliminato in mezzo sparisce anche dai Preferiti (cascade),
        // quindi ogni Preferito punta a Capi presenti nel JSON.
        val capi = db.capoDao().tutti().first()
        val preferiti = db.preferitoDao().tutti().first()
        // Un file sparito dal disco non è recuperabile: il Capo esce senza foto, così l'archivio resta coerente.
        val capiCoerenti = capi.map { if (it.percorsoFoto != null && !foto.file(it.percorsoFoto).isFile) it.copy(percorsoFoto = null) else it }
        val json = JSONObject()
            .put("versione", VERSIONE)
            .put("capi", JSONArray(capiCoerenti.map { it.json() }))
            .put("preferiti", JSONArray(preferiti.map { it.json() }))
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry(NOME_JSON))
            zip.write(json.toString().encodeToByteArray())
            capiCoerenti.mapNotNull { it.percorsoFoto }.distinct().forEach { nome ->
                zip.putNextEntry(ZipEntry("${Foto.CARTELLA}/$nome"))
                foto.file(nome).inputStream().use { it.copyTo(zip) }
            }
        }
    }

    private fun Capo.json() = JSONObject()
        .put("id", id)
        .put("categoria", categoria.name)
        .put("tipo", tipo)
        .put("colorePrincipale", colorePrincipale)
        .put("coloreSecondario", coloreSecondario ?: JSONObject.NULL)
        .put("motivo", motivo.name)
        .put("stagione", stagione.name)
        .put("taglia", taglia ?: JSONObject.NULL)
        .put("percorsoFoto", percorsoFoto ?: JSONObject.NULL)
        .put("creatoIl", creatoIl)

    private fun PreferitoConCapi.json() = JSONObject()
        .put("id", preferito.id)
        .put("nome", preferito.nome ?: JSONObject.NULL)
        .put("spiegazione", preferito.spiegazione)
        .put("creatoIl", preferito.creatoIl)
        .put("capi", JSONArray(capi.map { it.id }))

    companion object {
        const val VERSIONE = 1
        const val NOME_JSON = "guardaroba.json"
    }
}
