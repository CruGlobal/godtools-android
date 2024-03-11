package org.cru.godtools.ui.tools

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import org.cru.godtools.downloadmanager.DownloadProgress
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.junit.Rule

class SquareToolCardPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi()

    private val toolState = ToolCard.State(
        tool = randomTool(
            name = "Tool Title",
            category = Tool.CATEGORY_GOSPEL,
        ),
        banner = mockk(),
        secondLanguage = Language(Locale.FRENCH),
        secondTranslation = randomTranslation()
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
    fun `SquareToolCard() - Default`() {
        paparazzi.snapshot {
            GodToolsTheme(disableDagger = true) {
                Box {
                    SquareToolCard(
                        state = toolState,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }

    @Test
    fun `SquareToolCard() - Downloading`() {
        paparazzi.snapshot {
            GodToolsTheme(disableDagger = true) {
                Box {
                    SquareToolCard(
                        state = toolState.copy(
                            downloadProgress = DownloadProgress(2, 5)
                        ),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }

    @Test
    fun `SquareToolCard() - Show Second Language`() {
        paparazzi.snapshot {
            GodToolsTheme(disableDagger = true) {
                Box {
                    SquareToolCard(
                        state = toolState,
                        showSecondLanguage = true,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}
