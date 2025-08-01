package org.cru.godtools.ui.tools

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.cru.godtools.model.Language
import org.cru.godtools.ui.BasePaparazziTest
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assume.assumeThat
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class VariantToolCardPaparazziTest(
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(nightMode = nightMode, accessibilityMode = accessibilityMode) {
    @BeforeTest
    @OptIn(ExperimentalCoilApi::class, ExperimentalCoroutinesApi::class)
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val file = Drawable.createFromStream(javaClass.getResourceAsStream("banner.jpg"), "banner.jpg")!!
        Coil.setImageLoader(
            ImageLoader.Builder(paparazzi.context)
                .components {
                    add(
                        FakeImageLoaderEngine.Builder()
                            .intercept(ToolCardStateTestData.banner, file)
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
    fun `VariantToolCard() - Default`() = centerInSnapshot(Modifier.fillMaxSize()) {
        VariantToolCard(ToolCardStateTestData.tool)
    }

    @Test
    fun `VariantToolCard() - Selected`() = centerInSnapshot(Modifier.fillMaxSize()) {
        VariantToolCard(ToolCardStateTestData.tool, isSelected = true)
    }

    @Test
    fun `VariantToolCard() - No second Language`() = centerInSnapshot(Modifier.fillMaxSize()) {
        VariantToolCard(ToolCardStateTestData.tool.copy(secondLanguage = null, secondLanguageAvailable = false))
    }

    @Test
    fun `VariantToolCard() - App Language Not Available`() = centerInSnapshot(Modifier.fillMaxSize()) {
        VariantToolCard(ToolCardStateTestData.tool.copy(appLanguageAvailable = false))
    }

    @Test
    fun `VariantToolCard() - Second Language Not Available`() = centerInSnapshot(Modifier.fillMaxSize()) {
        VariantToolCard(ToolCardStateTestData.tool.copy(secondLanguageAvailable = false))
    }

    @Test
    fun `VariantToolCard() - GT-2362 - Second Language Matches App Language`() {
        assumeThat(accessibilityMode, equalTo(AccessibilityMode.NO_ACCESSIBILITY))
        assumeThat(nightMode, equalTo(NightMode.NOTNIGHT))

        centerInSnapshot(Modifier.fillMaxSize()) {
            VariantToolCard(
                ToolCardStateTestData.tool.copy(
                    appLanguage = Language(Locale.ENGLISH),
                    appLanguageAvailable = true,
                    secondLanguage = Language(Locale.ENGLISH),
                    secondLanguageAvailable = true,
                )
            )
        }
    }

    @Test
    fun `VariantToolCard() - GT-2365 - Short Language Name`() {
        assumeThat(accessibilityMode, equalTo(AccessibilityMode.NO_ACCESSIBILITY))
        assumeThat(nightMode, equalTo(NightMode.NOTNIGHT))

        centerInSnapshot(Modifier.fillMaxSize()) {
            VariantToolCard(ToolCardStateTestData.tool.copy(secondLanguage = Language(Locale("cs"))))
        }
    }
}
