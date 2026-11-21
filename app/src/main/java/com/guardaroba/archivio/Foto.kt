package com.guardaroba.archivio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.UUID
import kotlin.math.max

/**
 * Le foto dei Capi nello storage interno (ADR-0003): solo la versione compressa, mai l'originale.
 * Un Capo ne tiene il nome in [Capo.percorsoFoto]; [elimina] è l'unico punto che cancella i file.
 */
class Foto(context: Context) {
    private val cartella = File(context.filesDir, CARTELLA)

    fun file(nome: String) = File(cartella, nome)

    /**
     * Legge la Foto da [apri] (chiamato più volte, uno stream nuovo ogni volta), la raddrizza secondo l'EXIF,
     * la riduce a [LATO] px sul lato lungo e la salva in WebP. Ritorna il nome del file.
     */
    fun comprimi(apri: () -> InputStream): String {
        val bordi = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        apri().use { BitmapFactory.decodeStream(it, null, bordi) }
        val latoLungo = max(bordi.outWidth, bordi.outHeight)
        if (latoLungo <= 0) throw IOException("Foto non leggibile")
        // Decodifica già sottocampionata, per non tenere in memoria i 12+ Mpx dell'originale.
        var campione = 1
        while (latoLungo / (campione * 2) >= LATO) campione *= 2
        val letta = apri().use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = campione }) }
            ?: throw IOException("Foto non leggibile")

        val scala = minOf(1f, LATO.toFloat() / max(letta.width, letta.height))
        val matrice = Matrix().apply { postScale(scala, scala); postRotate(gradiExif(apri)) }
        val finale = Bitmap.createBitmap(letta, 0, 0, letta.width, letta.height, matrice, true)

        val webp = ByteArrayOutputStream()
        if (!finale.compress(WEBP, QUALITA, webp)) throw IOException("Compressione WebP fallita")
        return scrivi(webp.toByteArray())
    }

    /** Salva [bytes] (una WebP) come Foto nuova, sotto un nome che non esce dalla cartella. Ritorna il nome del file. */
    fun scrivi(bytes: ByteArray): String {
        cartella.mkdirs()
        val nome = "${UUID.randomUUID()}.webp"
        try {
            file(nome).writeBytes(bytes)
        } catch (e: Exception) {
            elimina(nome) // una scrittura a metà (disco pieno) non deve restare
            throw e
        }
        return nome
    }

    /** I nomi di tutte le foto sul disco. */
    fun tutte(): List<String> = cartella.list()?.toList().orEmpty()

    /** Cancella la foto [nome], se c'è. */
    fun elimina(nome: String?) {
        if (nome != null) file(nome).delete()
    }

    // shortcut: solo le rotazioni; le orientazioni specchiate (rare, fotocamere frontali) restano specchiate.
    private fun gradiExif(apri: () -> InputStream): Float {
        // Un EXIF assente o rovinato non deve far perdere la foto: la si tiene com'è.
        val orientamento = runCatching {
            apri().use { ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) }
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        return when (orientamento) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }

    companion object {
        const val CARTELLA = "foto"
        const val LATO = 720
        const val QUALITA = 75

        @Suppress("DEPRECATION") // WEBP_LOSSY esiste solo da API 30; prima WEBP è già lossy con qualità < 100
        private val WEBP = if (Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
    }
}
