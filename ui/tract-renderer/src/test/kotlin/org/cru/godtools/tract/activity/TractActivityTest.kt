package org.cru.godtools.tract.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.viewModels
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import java.util.Locale
import javax.inject.Inject
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.flow.MutableSharedFlow
import org.cru.godtools.base.EXTRA_LANGUAGES
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.HOST_DYNALINKS
import org.cru.godtools.base.HOST_GODTOOLSAPP_COM
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivityDataModel
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.ui.createTractActivityIntent
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.tool.tract.BuildConfig.HOST_GODTOOLS_CUSTOM_URI
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val TOOL = "test"
private const val TOOL2 = "tool2"

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class TractActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    @Inject
    lateinit var manifestManager: ManifestManager
    @Inject
    lateinit var translationsRepository: TranslationsRepository

    private fun <R> scenario(
        intent: Intent = context.createTractActivityIntent(TOOL, Locale.ENGLISH),
        block: (ActivityScenario<TractActivity>) -> R
    ) = ActivityScenario.launch<TractActivity>(intent).use(block)
    private fun <R> deepLinkScenario(uri: Uri, block: (ActivityScenario<TractActivity>) -> R) =
        scenario(Intent(Intent.ACTION_VIEW, uri), block)

    @BeforeTest
    fun setup() {
        hiltRule.inject()
    }

    // region Intent Processing
    @Test
    fun `processIntent() - Valid direct`() {
        scenario(context.createTractActivityIntent(TOOL, Locale.ENGLISH)) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(Locale.ENGLISH, it.dataModel.primaryLocales.value!!.single())
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Valid direct with parallel language`() {
        scenario(context.createTractActivityIntent(TOOL, Locale.ENGLISH, Locale.FRENCH)) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(Locale.ENGLISH, it.dataModel.primaryLocales.value!!.single())
                assertEquals(Locale.FRENCH, it.dataModel.parallelLocales.value!!.single())
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Invalid missing tool`() {
        scenario(context.createTractActivityIntent(TOOL, Locale.ENGLISH).apply { removeExtra(EXTRA_TOOL) }) {
            assertEquals(Lifecycle.State.DESTROYED, it.state)
        }
    }

    @Test
    fun `processIntent() - Invalid missing locales`() {
        scenario(context.createTractActivityIntent(TOOL, Locale.ENGLISH).apply { removeExtra(EXTRA_LANGUAGES) }) {
            assertEquals(Lifecycle.State.DESTROYED, it.state)
        }
    }

    @Test
    fun `processIntent() - Deep Link - knowgod_com`() {
        everyGetManifestFlow() returns MutableSharedFlow()

        deepLinkScenario(Uri.parse("https://knowgod.com/fr/tool/v1/kgp?primaryLanguage=en&parallelLanguage=es,fr")) {
            it.onActivity {
                assertEquals("kgp", it.dataModel.toolCode.value)
                assertEquals(Locale.ENGLISH, it.dataModel.primaryLocales.value!!.single())
                assertEquals(listOf(Locale("es"), Locale.FRENCH), it.dataModel.parallelLocales.value)
                assertEquals(Locale.FRENCH, it.dataModel.activeLocale.value)
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Deep Link - knowgod_com - With Page Num`() {
        everyGetManifestFlow() returns MutableSharedFlow()

        deepLinkScenario(Uri.parse("https://knowgod.com/fr/tool/v1/kgp/3?primaryLanguage=en&parallelLanguage=es,fr")) {
            it.onActivity {
                assertEquals("kgp", it.dataModel.toolCode.value)
                assertEquals(Locale.ENGLISH, it.dataModel.primaryLocales.value!!.single())
                assertEquals(listOf(Locale("es"), Locale.FRENCH), it.dataModel.parallelLocales.value)
                assertEquals(Locale.FRENCH, it.dataModel.activeLocale.value)
                assertEquals(3, it.initialPage)
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Deep Link - dynalinks`() {
        deepLinkScenario(Uri.parse("https://$HOST_DYNALINKS/deeplink/tool/tract/$TOOL/fr")) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(Locale.FRENCH, it.dataModel.primaryLocales.value!!.single())
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Deep Link - dynalinks - Missing Language`() {
        deepLinkScenario(Uri.parse("https://$HOST_DYNALINKS/deeplink/tool/tract/$TOOL/")) {
            assertEquals(Lifecycle.State.DESTROYED, it.state)
        }
    }

    @Test
    fun `processIntent() - Deep Link - dynalinks - With Page Num`() {
        deepLinkScenario(Uri.parse("https://$HOST_DYNALINKS/deeplink/tool/tract/$TOOL/fr/3")) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(Locale.FRENCH, it.dataModel.primaryLocales.value!!.single())
                assertEquals(3, it.initialPage)
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Deep Link - godtoolsapp_com`() {
        deepLinkScenario(Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/tool/tract/$TOOL/fr")) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(Locale.FRENCH, it.dataModel.primaryLocales.value!!.single())
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Deep Link - godtoolsapp_com - Missing Language`() {
        deepLinkScenario(Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/tool/tract/$TOOL/")) {
            assertEquals(Lifecycle.State.DESTROYED, it.state)
        }
    }

    @Test
    fun `processIntent() - Deep Link - godtoolsapp_com - With Page Num`() {
        deepLinkScenario(Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/tool/tract/$TOOL/fr/3")) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(Locale.FRENCH, it.dataModel.primaryLocales.value!!.single())
                assertEquals(3, it.initialPage)
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Deep Link - Custom Uri Scheme`() {
        deepLinkScenario(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/tool/tract/$TOOL/fr")) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(Locale.FRENCH, it.dataModel.primaryLocales.value!!.single())
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Deep Link - Custom Uri Scheme - Missing Language`() {
        deepLinkScenario(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/tool/tract/$TOOL/")) {
            assertEquals(Lifecycle.State.DESTROYED, it.state)
        }
    }

    @Test
    fun `processIntent() - Deep Link - Custom Uri Scheme - With Page Num`() {
        deepLinkScenario(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/tool/tract/$TOOL/fr/3")) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(Locale.FRENCH, it.dataModel.primaryLocales.value!!.single())
                assertEquals(3, it.initialPage)
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Preserve tool and language changes - Direct`() {
        scenario(context.createTractActivityIntent(TOOL, Locale.ENGLISH, Locale.FRENCH)) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(Locale.ENGLISH, it.dataModel.primaryLocales.value!!.single())
                assertEquals(Locale.FRENCH, it.dataModel.parallelLocales.value!!.single())
                assertFalse(it.isFinishing)
            }

            // update tool & languages
            it.onActivity {
                it.dataModel.toolCode.value = TOOL2
                it.dataModel.primaryLocales.value = listOf(Locale.GERMAN)
                it.dataModel.parallelLocales.value = listOf(Locale.CHINESE)
            }

            // recreate activity, which should keep previously set tool and locales
            it.recreate()
            it.onActivity {
                assertEquals(TOOL2, it.dataModel.toolCode.value)
                assertEquals(Locale.GERMAN, it.dataModel.primaryLocales.value!!.single())
                assertEquals(Locale.CHINESE, it.dataModel.parallelLocales.value!!.single())
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Preserve tool and language changes - Deep Link`() {
        deepLinkScenario(Uri.parse("https://knowgod.com/fr/tool/v1/kgp?primaryLanguage=en&parallelLanguage=es,fr")) {
            // initially parse deep link
            it.onActivity {
                assertEquals("kgp", it.dataModel.toolCode.value)
                assertEquals(Locale.ENGLISH, it.dataModel.primaryLocales.value!!.single())
                assertEquals(listOf(Locale("es"), Locale.FRENCH), it.dataModel.parallelLocales.value)
                assertFalse(it.isFinishing)
            }

            // update tool & languages
            it.onActivity {
                it.dataModel.toolCode.value = TOOL2
                it.dataModel.primaryLocales.value = listOf(Locale.GERMAN)
                it.dataModel.parallelLocales.value = listOf(Locale.CHINESE)
            }

            // recreate activity, which should keep previously set tool and locales
            it.recreate()
            it.onActivity {
                assertEquals(TOOL2, it.dataModel.toolCode.value)
                assertEquals(Locale.GERMAN, it.dataModel.primaryLocales.value!!.single())
                assertEquals(Locale.CHINESE, it.dataModel.parallelLocales.value!!.single())
                assertFalse(it.isFinishing)
            }
        }
    }
    // endregion Intent Processing

    private val TractActivity.dataModel get() = viewModels<MultiLanguageToolActivityDataModel>().value

    private fun everyGetManifestFlow(tool: String? = null, locale: Locale? = null) =
        every { (manifestManager.getLatestPublishedManifestFlow(tool ?: any(), locale ?: any())) }
}
