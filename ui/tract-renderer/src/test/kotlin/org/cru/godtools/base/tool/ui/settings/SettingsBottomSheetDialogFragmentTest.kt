package org.cru.godtools.base.tool.ui.settings

import android.content.Context
import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.coEvery
import io.mockk.coVerify
import java.util.Locale
import javax.inject.Inject
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import org.cru.godtools.base.ui.createTractActivityIntent
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.tract.activity.TractActivity
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private const val TOOL = "test"

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class SettingsBottomSheetDialogFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    @Inject
    lateinit var toolsRepository: ToolsRepository

    @BeforeTest
    fun setup() {
        hiltRule.inject()
    }

    // region swapLanguages()
    @Test
    fun `swapLanguages() - Tool locales are stored even if the sheet is dismissed`() {
        val dbWriterAvailable = CompletableDeferred<Unit>()
        var toolLocalesStored = false
        coEvery { toolsRepository.updateToolLocales(TOOL, Locale.FRENCH, Locale.ENGLISH) } coAnswers {
            dbWriterAvailable.await()
            toolLocalesStored = true
        }

        val intent = context.createTractActivityIntent(TOOL, Locale.ENGLISH, Locale.FRENCH)
        ActivityScenario.launch<TractActivity>(intent).use {
            it.onActivity {
                val fragment = SettingsBottomSheetDialogFragment(saveLanguageSettings = true)
                fragment.showNow(it.supportFragmentManager, null)
                fragment.swapLanguages()
                fragment.dismissNow()
            }

            dbWriterAvailable.complete(Unit)
            shadowOf(Looper.getMainLooper()).idle()
            coVerify { toolsRepository.updateToolLocales(TOOL, Locale.FRENCH, Locale.ENGLISH) }
            assertTrue(toolLocalesStored)
        }
    }
    // endregion swapLanguages()
}
