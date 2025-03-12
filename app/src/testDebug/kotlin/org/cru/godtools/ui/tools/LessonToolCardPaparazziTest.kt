package org.cru.godtools.ui.tools

import android.graphics.drawable.Drawable
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
import org.cru.godtools.model.randomTranslation
import org.cru.godtools.ui.BasePaparazziTest
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class LessonToolCardPaparazziTest(
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(nightMode = nightMode, accessibilityMode = accessibilityMode) {
    private val toolState = ToolCardStateTestData.tool

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
    fun `LessonToolCard() - Default`() = centerInSnapshot {
        LessonToolCard(toolState, showLanguage = true, showProgress = true)
    }

    @Test
    fun `LessonToolCard() - Long Title`() = centerInSnapshot {
        LessonToolCard(
            toolState.copy(
                translation = randomTranslation(
                    name = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt"
                )
            ),
            showLanguage = true,
            showProgress = true,
        )
    }

    @Test
    fun `LessonToolCard() - Tool Language - RTL Language`() = centerInSnapshot {
        LessonToolCard(
            toolState.copy(
                translation = randomTranslation(
                    languageCode = Locale.forLanguageTag("ar"),
                    name = "كيف تظهر الإنجيل بحياتك"
                ),
                language = Language(Locale.forLanguageTag("ar")),
            ),
            showLanguage = true,
            showProgress = true,
        )
    }

    @Test
    fun `LessonToolCard() - Progress - Completed`() = centerInSnapshot {
        LessonToolCard(toolState.copy(progress = ToolCard.State.Progress.Completed), showProgress = true)
    }

    @Test
    fun `LessonToolCard() - Not Available`() = centerInSnapshot {
        LessonToolCard(toolState.copy(languageAvailable = false), showLanguage = true)
    }

    @Test
    fun `LessonToolCard() - Hide Language`() = centerInSnapshot {
        LessonToolCard(toolState, showLanguage = false, showProgress = true)
    }

    @Test
    fun `LessonToolCard() - Hide Progress`() = centerInSnapshot {
        LessonToolCard(toolState, showLanguage = true, showProgress = false)
    }
}
