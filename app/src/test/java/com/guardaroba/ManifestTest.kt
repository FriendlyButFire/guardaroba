package com.guardaroba

import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManifestTest {
    @Test
    fun `l'app non dichiara il permesso INTERNET`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val permessi = context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions.orEmpty()
        assertFalse(android.Manifest.permission.INTERNET in permessi)
    }
}
