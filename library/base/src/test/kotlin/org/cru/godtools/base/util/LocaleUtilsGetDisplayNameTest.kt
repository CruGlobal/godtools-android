package org.cru.godtools.base.util

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.MockKStubScope
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val PACKAGE_NAME = "packageName"

@RunWith(AndroidJUnit4::class)
@Category(AndroidJUnit4::class)
@Config(application = Application::class)
class LocaleUtilsGetDisplayNameTest {
    lateinit var context: Context

    @Before
    fun setup() {
        context = mockk {
            every { packageName } returns PACKAGE_NAME
            every { resources } returns mockk {
                every { configuration } returns Configuration()
                every { getIdentifier(any(), "string", PACKAGE_NAME) } returns 0
            }
            every { createConfigurationContext(any()) } returns this
        }
    }

    @Test
    fun preferLanguageNameString() {
        everyGetLanguageNameStringRes(Locale.ENGLISH) returns "Language Name String"

        assertEquals("Language Name String", Locale.ENGLISH.getDisplayName(context, defaultName = "invalid"))
        verify(inverse = true) { context.createConfigurationContext(any()) }
    }

    @Test
    fun preferLanguageNameStringInDifferentLocale() {
        everyGetLanguageNameStringRes(Locale.ENGLISH) returns "Language Name String"

        assertEquals(
            "Language Name String",
            Locale.ENGLISH.getDisplayName(context, defaultName = "invalid", inLocale = Locale.FRENCH)
        )
        verify(exactly = 1) { context.createConfigurationContext(match { it.locale == Locale.FRENCH }) }
    }

    @Test
    fun preferSystemDisplayName() {
        assertEquals("English", Locale.ENGLISH.getDisplayName(context = context, defaultName = "invalid"))
    }

    @Test
    fun preferDefaultNameParameter() {
        assertEquals("Parameter", Locale("x").getDisplayName(context = context, defaultName = "Parameter"))
    }

    @Test
    fun fallbackToLanguageCode() {
        assertEquals("x", Locale("x").getDisplayName(context = context))
    }

    private fun everyGetLanguageNameStringRes(locale: Locale): MockKStubScope<String, String> {
        val id = Random.nextInt(1, Int.MAX_VALUE)
        val resources = context.resources
        every {
            resources.getIdentifier(
                "$STRING_RES_LANGUAGE_NAME_PREFIX${locale.toString().lowercase(Locale.ENGLISH)}", "string", PACKAGE_NAME
            )
        } returns id
        return every { resources.getString(id) }
    }
}
