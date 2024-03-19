package org.cru.godtools.ui.tools

import android.graphics.drawable.Drawable
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
import org.cru.godtools.downloadmanager.DownloadProgress
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.ui.BasePaparazziTest

class SquareToolCardPaparazziTest : BasePaparazziTest() {
    private val toolState = ToolCard.State(
        tool = randomTool(
            name = "Tool Title",
            category = Tool.CATEGORY_GOSPEL,
        ),
        banner = mockk(),
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
    fun `SquareToolCard() - Default`() = centerInSnapshot { SquareToolCard(toolState) }

    @Test
    fun `SquareToolCard() - Downloading`() = centerInSnapshot {
        SquareToolCard(toolState.copy(downloadProgress = DownloadProgress(2, 5)))
    }

    @Test
    fun `SquareToolCard() - Show Second Language`() = centerInSnapshot {
        SquareToolCard(toolState, showSecondLanguage = true)
    }
}
