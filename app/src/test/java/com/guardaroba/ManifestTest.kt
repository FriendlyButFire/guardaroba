package com.guardaroba

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManifestTest {
    private val permessi = ApplicationProvider.getApplicationContext<Context>().run {
        packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS).requestedPermissions.orEmpty().toList()
    }

    @Test
    fun `l'app non dichiara il permesso INTERNET`() {
        assertFalse(android.Manifest.permission.INTERNET in permessi)
    }

    @Test
    fun `le foto non chiedono permessi di storage né della fotocamera`() {
        val vietati = listOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.CAMERA,
        )
        assertEquals(emptyList<String>(), permessi.filter { it in vietati })
    }

    @Test
    fun `il backup automatico di Android è attivo`() {
        val info = ApplicationProvider.getApplicationContext<Context>().applicationInfo
        assertTrue(info.flags and ApplicationInfo.FLAG_ALLOW_BACKUP != 0)
    }
}
