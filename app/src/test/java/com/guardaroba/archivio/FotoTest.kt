package com.guardaroba.archivio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FotoTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val foto = Foto(context)

    /** Uno scatto di prova 4000×3000 in JPEG, come lo lascerebbe la fotocamera. */
    private fun scatto(orientamento: Int = ExifInterface.ORIENTATION_NORMAL): File {
        val bitmap = Bitmap.createBitmap(4000, 3000, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).run {
            drawPaint(Paint().apply { shader = LinearGradient(0f, 0f, 4000f, 3000f, Color.BLUE, Color.YELLOW, Shader.TileMode.CLAMP) })
            drawCircle(2000f, 1500f, 900f, Paint().apply { color = Color.RED })
        }
        val file = File(context.cacheDir, "scatto.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
        ExifInterface(file.path).run {
            setAttribute(ExifInterface.TAG_ORIENTATION, orientamento.toString())
            saveAttributes()
        }
        return file
    }

    @Test
    fun `una foto grande diventa una WebP di al massimo 720 px sul lato lungo e 60 KB`() {
        val originale = scatto()

        val compressa = foto.file(foto.comprimi { originale.inputStream() })

        val bordi = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(compressa.path, bordi)
        assertEquals(720 to 540, bordi.outWidth to bordi.outHeight)
        assertEquals("image/webp", bordi.outMimeType)
        assertTrue("${compressa.length()} byte", compressa.length() <= 60 * 1024)
    }

    @Test
    fun `una foto scattata in verticale resta verticale`() {
        val originale = scatto(ExifInterface.ORIENTATION_ROTATE_90)

        val compressa = foto.file(foto.comprimi { originale.inputStream() })

        val bordi = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(compressa.path, bordi)
        assertEquals(540 to 720, bordi.outWidth to bordi.outHeight)
    }

    @Test
    fun `scrivere dei byte dà ogni volta una Foto nuova con quei byte`() {
        val bytes = byteArrayOf(1, 2, 3)

        val prima = foto.scrivi(bytes)
        val seconda = foto.scrivi(bytes)

        assertTrue(prima != seconda)
        assertTrue(foto.tutte().containsAll(listOf(prima, seconda)))
        assertTrue(bytes.contentEquals(foto.file(seconda).readBytes()))
    }
}
