package com.guardaroba.archivio

import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Importa con la rete (ADR-0015): prima di sostituire il Guardaroba ne salva una copia in [copia], fuori dal backup automatico;
 * [ripristina] la rimette, salvando a sua volta quello che c'era. Si tiene solo l'ultima copia.
 */
class Sostituzione(private val esportazione: Esportazione, private val importazione: Importazione, private val copia: File) {

    /** Quando è stata fatta la copia del Guardaroba di prima, se c'è. */
    fun copiaDel(): Long? = copia.takeIf { it.isFile }?.lastModified()

    /**
     * Salva la copia del Guardaroba di adesso e lo sostituisce con lo ZIP di [input]. La copia va al suo posto prima dell'import:
     * un errore dopo che l'import ha scritto il database lascia comunque la rete; un import che fallisce prima la lascia uguale
     * al Guardaroba di adesso, che non è cambiato.
     */
    suspend fun sostituisci(input: InputStream) {
        val nuova = File(copia.path + ".tmp")
        try {
            nuova.outputStream().use { esportazione.esporta(it) }
            Files.move(nuova.toPath(), copia.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } finally {
            nuova.delete()
        }
        importazione.importa(input)
    }

    // shortcut: legge la copia in memoria (al più qualche decina di MB, ADR-0003) perché la nuova copia la sostituisce
    // mentre sarebbe ancora aperta; con Guardaroba molto più grandi, spostarla prima su un file a parte.
    suspend fun ripristina() = sostituisci(copia.readBytes().inputStream())
}
