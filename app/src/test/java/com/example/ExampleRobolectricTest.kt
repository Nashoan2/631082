package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("المملكة للإلكترونيات", appName)
  }

  @Test
  fun `auto backup is disabled by default on first install and can be activated`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    // Clear any existing preference to simulate a first-time fresh install
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit().remove("isAutoDailyBackupEnabled").commit()

    val repository = com.example.data.InvoiceRepository(context)
    org.junit.Assert.assertFalse(repository.isAutoDailyBackupEnabled)

    repository.setAutoDailyBackupEnabled(true)
    org.junit.Assert.assertTrue(repository.isAutoDailyBackupEnabled)

    repository.setAutoDailyBackupEnabled(false)
    org.junit.Assert.assertFalse(repository.isAutoDailyBackupEnabled)
  }
}
