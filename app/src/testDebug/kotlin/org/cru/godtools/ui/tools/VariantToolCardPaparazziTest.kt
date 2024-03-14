package org.cru.godtools.ui.tools

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import app.cash.paparazzi.Paparazzi
import coil.Coil
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.test.FakeImageLoaderEngine
import io.mockk.mockk
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.model.Language
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.junit.Rule

class VariantToolCardPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi()

    private val toolState = ToolCard.State(
        tool = randomTool(
            name = "Tool Title",
            description = "Description of tool",
        ),
        banner = mockk(),
        availableLanguages = 1234,
        appLanguage = Language(Locale.ENGLISH),
        appTranslation = randomTranslation(),
        secondLanguage = Language(Locale.FRENCH),
        secondLanguageAvailable = true,
    )

    @BeforeTest
    @OptIn(ExperimentalCoilApi::class, ExperimentalCoroutinesApi::class)
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val banner = Drawable.createFromStream(javaClass.getResourceAsStream("banner.jpg"), "banner.jpg")!!
        Coil.setImageLoader(
            ImageLoader.Builder(paparazzi.context)
                .components {
                    add(
                        FakeImageLoaderEngine.Builder()
                            .intercept(toolState.banner!!, banner)
                            .build()
                    )
                }
                .build()
        )
    }

    @AfterTest
    @OptIn(ExperimentalCoroutinesApi::class)
    fun cleanup() {
        Coil.reset()
        Dispatchers.resetMain()
    }

    @Test
    fun `VariantToolCard() - Default`() {
        paparazzi.snapshot {
            GodToolsTheme(disableDagger = true) {
                Box(contentAlignment = Alignment.Center) {
                    VariantToolCard(state = toolState)
                }
            }
        }
    }

    @Test
    fun `VariantToolCard() - Selected`() {
        paparazzi.snapshot {
            GodToolsTheme(disableDagger = true) {
                Box(contentAlignment = Alignment.Center) {
                    VariantToolCard(state = toolState, isSelected = true)
                }
            }
        }
    }

    @Test
    fun `VariantToolCard() - No second Language`() {
        paparazzi.snapshot {
            GodToolsTheme(disableDagger = true) {
                Box(contentAlignment = Alignment.Center) {
                    VariantToolCard(state = toolState.copy(secondLanguage = null, secondLanguageAvailable = false))
                }
            }
        }
    }

    @Test
    fun `VariantToolCard() - App Language Not Available`() {
        paparazzi.snapshot {
            GodToolsTheme(disableDagger = true) {
                Box(contentAlignment = Alignment.Center) {
                    VariantToolCard(state = toolState.copy(appTranslation = null))
                }
            }
        }
    }

    @Test
    fun `VariantToolCard() - Second Language Not Available`() {
        paparazzi.snapshot {
            GodToolsTheme(disableDagger = true) {
                Box(contentAlignment = Alignment.Center) {
                    VariantToolCard(state = toolState.copy(secondLanguageAvailable = false))
                }
            }
        }
    }
}
