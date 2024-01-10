package org.cru.godtools.base.ui.util

import android.os.Build
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NEWEST_SDK
import org.robolectric.annotation.Config.OLDEST_SDK

@RunWith(AndroidJUnit4::class)
@Config(sdk = [OLDEST_SDK, Build.VERSION_CODES.LOLLIPOP_MR1, Build.VERSION_CODES.M, NEWEST_SDK])
class LocaleTypefaceUtilsTest {
    private val baseTextStyle = TextStyle(fontFamily = FontFamily.Default)

    private val localeSinhala = Locale("si")
    private val localeSinhalaRegion = Locale("si", "US")
    private val localeTibetan = Locale("bo")
    private val localeTibetanRegion = Locale("bo", "US")

    @Test
    fun `getFontFamilyOrNull() - Locales without compat font`() {
        assertNull(Locale.ENGLISH.getFontFamilyOrNull())
        assertNull(Locale.FRENCH.getFontFamilyOrNull())
    }

    @Test
    @Config(sdk = [OLDEST_SDK, Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `getFontFamilyOrNull() - Sinhala Compat`() {
        assertEquals(FONT_SINHALA, localeSinhala.getFontFamilyOrNull())
        assertEquals(FONT_SINHALA, localeSinhalaRegion.getFontFamilyOrNull())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M, NEWEST_SDK])
    fun `getFontFamilyOrNull() - Sinhala Native`() {
        assertNull(localeSinhala.getFontFamilyOrNull())
        assertNull(localeSinhalaRegion.getFontFamilyOrNull())
    }

    @Test
    @Config(sdk = [OLDEST_SDK, Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `getFontFamilyOrNull() - Tibetan Compat`() {
        assertEquals(FONT_TIBETAN, localeTibetan.getFontFamilyOrNull())
        assertEquals(FONT_TIBETAN, localeTibetanRegion.getFontFamilyOrNull())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M, NEWEST_SDK])
    fun `getFontFamilyOrNull() - Tibetan Native`() {
        assertNull(localeTibetan.getFontFamilyOrNull())
        assertNull(localeTibetanRegion.getFontFamilyOrNull())
    }

    @Test
    fun `withCompatFontFamilyFor() - Locales without compat font`() {
        assertEquals(baseTextStyle, baseTextStyle.withCompatFontFamilyFor(Locale.ENGLISH))
        assertEquals(baseTextStyle, baseTextStyle.withCompatFontFamilyFor(Locale.FRENCH))
    }

    @Test
    @Config(sdk = [OLDEST_SDK, Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `withCompatFontFamilyFor() - Sinhala Compat`() {
        with(baseTextStyle.withCompatFontFamilyFor(localeSinhala)) {
            assertNotEquals(baseTextStyle, this)
            assertEquals(FONT_SINHALA, fontFamily)
        }
        with(baseTextStyle.withCompatFontFamilyFor(localeSinhalaRegion)) {
            assertNotEquals(baseTextStyle, this)
            assertEquals(FONT_SINHALA, fontFamily)
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M, NEWEST_SDK])
    fun `withCompatFontFamilyFor() - Sinhala Native`() {
        assertEquals(baseTextStyle, baseTextStyle.withCompatFontFamilyFor(localeSinhala))
        assertEquals(baseTextStyle, baseTextStyle.withCompatFontFamilyFor(localeSinhalaRegion))
    }

    @Test
    @Config(sdk = [OLDEST_SDK, Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `withCompatFontFamilyFor() - Tibetan Compat`() {
        with(baseTextStyle.withCompatFontFamilyFor(localeTibetan)) {
            assertNotEquals(baseTextStyle, this)
            assertEquals(FONT_TIBETAN, fontFamily)
        }
        with(baseTextStyle.withCompatFontFamilyFor(localeTibetanRegion)) {
            assertNotEquals(baseTextStyle, this)
            assertEquals(FONT_TIBETAN, fontFamily)
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M, NEWEST_SDK])
    fun `withCompatFontFamilyFor() - Tibetan Native`() {
        assertEquals(baseTextStyle, baseTextStyle.withCompatFontFamilyFor(localeTibetan))
        assertEquals(baseTextStyle, baseTextStyle.withCompatFontFamilyFor(localeTibetanRegion))
    }
}
