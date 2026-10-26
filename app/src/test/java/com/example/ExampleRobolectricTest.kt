package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.pref.AppPreferences
import com.example.i18n.LanguageManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FCM Guard", appName)
    }

    @Test
    fun `test language translations`() {
        val viDashboard = LanguageManager.getString("tab_dashboard", "vi")
        assertEquals("Bảng điều khiển", viDashboard)

        val enDashboard = LanguageManager.getString("tab_dashboard", "en")
        assertEquals("Dashboard", enDashboard)
    }

    @Test
    fun `test app preferences default values`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = AppPreferences(context)
        assertTrue(prefs.isAutoGuardEnabled.value)
        assertEquals(30, prefs.backupIntervalMinutes.value)
    }
}
