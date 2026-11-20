package org.cru.godtools.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class AppLanguageTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Volatile
    private var contextAppLanguage: Locale = Locale.ENGLISH

    @BeforeTest
    fun setup() {
        mockkStatic("org.cru.godtools.base.AppLanguageKt")
        every { any<Context>().appLanguage } answers { contextAppLanguage }
    }

    @AfterTest
    fun cleanup() {
        unmockkStatic("org.cru.godtools.base.AppLanguageKt")
    }

    // region getAppLanguageFlow()
    @Test
    fun `getAppLanguageFlow() - emits updated language after notifyAppLanguageChanged()`() = runTest {
        context.getAppLanguageFlow().test {
            assertEquals(Locale.ENGLISH, awaitItem())

            contextAppLanguage = Locale.FRENCH
            notifyAppLanguageChanged()
            assertEquals(Locale.FRENCH, awaitItem())
        }
    }

    @Test
    fun `getAppLanguageFlow() - emits updated language after a configuration change`() = runTest {
        context.getAppLanguageFlow().test {
            assertEquals(Locale.ENGLISH, awaitItem())

            contextAppLanguage = Locale.FRENCH
            context.onConfigurationChanged(Configuration(context.resources.configuration))
            assertEquals(Locale.FRENCH, awaitItem())
        }
    }

    @Test
    fun `getAppLanguageFlow() - emits updated language after an activity is created`() = runTest {
        context.getAppLanguageFlow().test {
            assertEquals(Locale.ENGLISH, awaitItem())

            contextAppLanguage = Locale.FRENCH
            Robolectric.buildActivity(Activity::class.java).create()
            assertEquals(Locale.FRENCH, awaitItem())
        }
    }

    @Test
    fun `getAppLanguageFlow() - does not poll for changes`() = runTest {
        context.getAppLanguageFlow().test {
            assertEquals(Locale.ENGLISH, awaitItem())

            contextAppLanguage = Locale.FRENCH
            withContext(Dispatchers.Default) { delay(100) }
            expectNoEvents()
        }
    }
    // endregion getAppLanguageFlow()
}
