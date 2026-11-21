package com.guardaroba.archivio

import androidx.room.withTransaction
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Motivo
import com.guardaroba.motore.PALETTE
import com.guardaroba.motore.Stagione
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream

/** Il file scelto non è un export del Guardaroba leggibile da questa versione; il Guardaroba attuale non è stato toccato. */
class ArchivioNonValido(messaggio: String, causa: Throwable? = null) : Exception(messaggio, causa)

/**
 * L'import di uno ZIP di [Esportazione] (ADR-0007), che sostituisce tutto il Guardaroba (ADR-0009).
 * Valida l'intero archivio prima di toccare i dati; un archivio non valido lancia [ArchivioNonValido].
 */
class Importazione(private val db: GuardarobaDatabase, private val foto: Foto) {

    /** Legge lo ZIP da [input] (senza chiuderlo) e, se valido, sostituisce il Guardaroba. Da chiamare fuori dal main thread. */
    suspend fun importa(input: InputStream) {
        // Le foto arrivano con un nome nuovo, accanto a quelle attuali: nessun file esistente viene toccato
        // finché la transazione non è riuscita, e chi esce a metà lascia al più dei file in più.
        val nuoveFoto = mutableMapOf<String, String>() // nome nello ZIP → nome sul disco
        val capi = try {
            val (capi, preferiti) = valida(leggi(input, nuoveFoto), nuoveFoto)
            // Una cancellazione all'uscita da withTransaction, a commit fatto, finirebbe nel catch e cancellerebbe foto in uso.
            withContext(NonCancellable) {
                db.withTransaction {
                    db.preferitoDao().eliminaTutti() // la cascata toglie anche preferito_capo
                    db.capoDao().eliminaTutti()
                    db.capoDao().inserisci(capi)
                    db.preferitoDao().inserisciPreferiti(preferiti.map { it.first })
                    db.preferitoDao().inserisci(preferiti.flatMap { (p, idCapi) -> idCapi.map { PreferitoCapo(p.id, it) } })
                }
            }
            capi
        } catch (e: Throwable) {
            nuoveFoto.values.forEach(foto::elimina)
            throw e
        }
        // Solo ora le foto del Guardaroba sostituito, quelle dello ZIP senza Capo e gli avanzi di import interrotti non servono più.
        // shortcut: cancella anche una foto appena compressa da un form non ancora salvato; l'import sta in Impostazioni, quindi non capita.
        val inUso = capi.mapNotNull { it.percorsoFoto }.toSet()
        foto.tutte().filterNot { it in inUso }.forEach(foto::elimina)
    }

    /** Legge le voci dello ZIP: ritorna il JSON e copia ogni foto sul disco, registrandola in [nuoveFoto]. */
    private fun leggi(input: InputStream, nuoveFoto: MutableMap<String, String>): JSONObject {
        val zip = ZipInputStream(input)
        var json: ByteArray? = null
        var restanti = MAX_TOTALE
        while (true) {
            val nome = leggendo { zip.nextEntry }?.name ?: break
            // Il nome serve solo come chiave: sul disco la foto va sotto un UUID, quindi non può uscire dalla cartella.
            val nomeFoto = nome.removePrefix("${Foto.CARTELLA}/").takeIf { it != nome && it.isNotEmpty() && it.none { c -> c == '/' || c == '\\' } }
            if (nome != Esportazione.NOME_JSON && nomeFoto == null) throw ArchivioNonValido("Voce estranea nello ZIP: $nome")
            val bytes = leggendo { zip.leggiAlPiù(minOf(MAX_VOCE, restanti)) }
            restanti -= bytes.size
            if (nomeFoto == null) {
                if (json != null) throw ArchivioNonValido("$nome ripetuto")
                json = bytes
            } else {
                if (nomeFoto in nuoveFoto) throw ArchivioNonValido("Foto $nomeFoto ripetuta")
                // shortcut: i byte non vengono decodificati; una Foto rovinata si vede vuota. Da controllare se capita.
                // Un errore di scrittura (disco pieno) non è colpa del file: resta una IOException, non [ArchivioNonValido].
                nuoveFoto[nomeFoto] = foto.scrivi(bytes)
            }
        }
        val testo = json ?: throw ArchivioNonValido("Manca ${Esportazione.NOME_JSON}")
        return try {
            JSONObject(testo.decodeToString())
        } catch (e: JSONException) {
            throw ArchivioNonValido("${Esportazione.NOME_JSON} rovinato", e)
        }
    }

    /** Controlla ogni campo e ritorna Capi e Preferiti (con gli id dei loro Capi), le foto già col nome sul disco. */
    private fun valida(json: JSONObject, nuoveFoto: Map<String, String>): Pair<List<Capo>, List<Pair<Preferito, Set<Long>>>> {
        val versione = json.intero("versione")
        if (versione != Esportazione.VERSIONE.toLong()) throw ArchivioNonValido("Versione del formato non supportata: $versione")
        val capi = json.lista("capi").map { c ->
            Capo(
                id = c.id(),
                categoria = c.enum<Categoria>("categoria"),
                tipo = c.testo("tipo"),
                colorePrincipale = c.colore("colorePrincipale"),
                coloreSecondario = if (c.nullo("coloreSecondario")) null else c.colore("coloreSecondario"),
                motivo = c.enum<Motivo>("motivo"),
                stagione = c.enum<Stagione>("stagione"),
                taglia = if (c.nullo("taglia")) null else c.testo("taglia"),
                percorsoFoto = if (c.nullo("percorsoFoto")) null else c.testo("percorsoFoto").let {
                    nuoveFoto[it] ?: throw ArchivioNonValido("Foto $it assente nello ZIP")
                },
                creatoIl = c.intero("creatoIl"),
            )
        }
        val idCapi = capi.map { it.id }.toSet()
        if (idCapi.size != capi.size) throw ArchivioNonValido("Id di Capo ripetuto")
        val preferiti = json.lista("preferiti").map { p ->
            val suoiCapi = p.elementi("capi").map { it.intero() }
            if (suoiCapi.isEmpty() || suoiCapi.toSet().size != suoiCapi.size || !idCapi.containsAll(suoiCapi)) {
                throw ArchivioNonValido("Preferito con Capi vuoti, ripetuti o assenti")
            }
            Preferito(
                id = p.id(),
                nome = if (p.nullo("nome")) null else p.testo("nome"),
                spiegazione = p.testo("spiegazione"),
                creatoIl = p.intero("creatoIl"),
            ) to suoiCapi.toSet()
        }
        if (preferiti.map { it.first.id }.toSet().size != preferiti.size) throw ArchivioNonValido("Id di Preferito ripetuto")
        // [PreferitoDao.salva] tiene un solo Preferito per insieme di Capi: l'archivio deve rispettarlo.
        if (preferiti.map { it.second }.toSet().size != preferiti.size) throw ArchivioNonValido("Due Preferiti con gli stessi Capi")
        return capi to preferiti
    }

    companion object {
        // Una foto compressa pesa ~60 KB e il JSON di ~400 Capi qualche centinaio di KB, il tutto ~25 MB (ADR-0003):
        // i limiti fermano solo file assurdi o ZIP bomb, prima che riempiano memoria e disco.
        private const val MAX_VOCE = 10 * 1024 * 1024
        private const val MAX_TOTALE = 200 * 1024 * 1024
    }
}

// Lettura stretta: org.json converte i tipi da solo ("1" → 1), qui un tipo sbagliato rende l'archivio non valido.

private fun JSONObject.campo(nome: String): Any = if (has(nome)) get(nome) else throw ArchivioNonValido("Manca il campo $nome")
private fun JSONObject.nullo(nome: String) = campo(nome) == JSONObject.NULL
private fun JSONObject.testo(nome: String) = campo(nome) as? String ?: throw ArchivioNonValido("$nome non è un testo")
private fun JSONObject.intero(nome: String) = campo(nome).intero()
private fun JSONObject.id() = intero("id").also { if (it <= 0) throw ArchivioNonValido("Id non valido: $it") }
private fun JSONObject.colore(nome: String) = testo(nome).also { id -> if (PALETTE.none { it.id == id }) throw ArchivioNonValido("Colore fuori Palette: $id") }
private inline fun <reified E : Enum<E>> JSONObject.enum(nome: String): E = testo(nome).let { valore ->
    enumValues<E>().firstOrNull { it.name == valore } ?: throw ArchivioNonValido("$nome sconosciuto: $valore")
}
private fun JSONObject.elementi(nome: String): List<Any> = (campo(nome) as? JSONArray ?: throw ArchivioNonValido("$nome non è una lista"))
    .let { a -> (0 until a.length()).map(a::get) }
private fun JSONObject.lista(nome: String): List<JSONObject> = elementi(nome).map { it as? JSONObject ?: throw ArchivioNonValido("Voce di $nome non valida") }
private fun Any.intero(): Long = when (this) {
    is Int -> toLong()
    is Long -> this
    else -> throw ArchivioNonValido("$this non è un intero")
}

/** Un errore di lettura dello ZIP vuol dire file rovinato o non ZIP. */
private inline fun <T> leggendo(blocco: () -> T): T =
    try { blocco() } catch (e: IOException) { throw ArchivioNonValido("File illeggibile o non ZIP", e) }

/** I byte della voce corrente; oltre [massimo] l'archivio non è valido. */
private fun InputStream.leggiAlPiù(massimo: Int): ByteArray {
    val out = ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (true) {
        val letti = read(buffer)
        if (letti < 0) return out.toByteArray()
        if (out.size() + letti > massimo) throw ArchivioNonValido("ZIP troppo grande")
        out.write(buffer, 0, letti)
    }
}
