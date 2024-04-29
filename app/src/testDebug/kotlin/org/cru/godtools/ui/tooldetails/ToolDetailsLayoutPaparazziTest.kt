package org.cru.godtools.ui.tooldetails

import android.graphics.drawable.Drawable
import app.cash.paparazzi.DeviceConfig
import coil.Coil
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.test.FakeImageLoaderEngine
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.cru.godtools.downloadmanager.DownloadProgress
import org.cru.godtools.model.Language
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.cru.godtools.ui.BasePaparazziTest
import org.cru.godtools.ui.drawer.DrawerMenuScreenStateTestData
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen.State
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardStateTestData
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class ToolDetailsLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    private val banner = ToolCardStateTestData.banner
    private val variants: ImmutableList<ToolCard.State> = persistentListOf(
        ToolCardStateTestData.tool.copy(secondLanguage = null),
        ToolCardStateTestData.tool.copy(secondLanguage = null)
    )

    private val state = State(
        tool = randomTool(
            detailsBannerYoutubeVideoId = null,
            shares = 123355,
            pendingShares = 101,
            isFavorite = false
        ),
        translation = randomTranslation(
            name = "Tool",
            description = "Description",
        ),
        banner = banner,
        drawerState = DrawerMenuScreenStateTestData.closed
    )

    @BeforeTest
    @OptIn(ExperimentalCoilApi::class, ExperimentalCoroutinesApi::class)
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val file = Drawable.createFromStream(javaClass.getResourceAsStream("../tools/banner.jpg"), "banner.jpg")!!
        Coil.setImageLoader(
            ImageLoader.Builder(paparazzi.context)
                .components {
                    add(
                        FakeImageLoaderEngine.Builder()
                            .intercept(banner, file)
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
    fun `ToolDetailsLayout()`() = snapshot { ToolDetailsLayout(state) }

    @Test
    fun `ToolDetailsLayout() - Drawer Open`() = snapshot {
        ToolDetailsLayout(state.copy(drawerState = DrawerMenuScreenStateTestData.open))
    }

    @Test
    fun `ToolDetailsLayout() - isFavorite=true`() = snapshot {
        ToolDetailsLayout(
            state.copy(
                tool = randomTool(
                    detailsBannerYoutubeVideoId = null,
                    shares = 123456,
                    pendingShares = 0,
                    isFavorite = true
                )
            )
        )
    }

    @Test
    fun `ToolDetailsLayout() - Downloading`() = snapshot {
        ToolDetailsLayout(state.copy(downloadProgress = DownloadProgress(2, 5)))
    }

    @Test
    fun `ToolDetailsLayout() - Variants`() = snapshot {
        ToolDetailsLayout(state.copy(variants = variants, pages = persistentListOf(ToolDetailsScreen.Page.VARIANTS)))
    }

    @Test
    fun `ToolDetailsLayout() - Second Language Available`() = snapshot {
        ToolDetailsLayout(
            state.copy(
                secondLanguage = Language(Locale.FRENCH),
                secondTranslation = randomTranslation(),
            )
        )
    }
}
