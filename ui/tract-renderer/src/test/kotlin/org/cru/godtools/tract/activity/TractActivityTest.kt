package org.cru.godtools.tract.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.ccci.gto.android.common.db.Query
import org.cru.godtools.base.EXTRA_LANGUAGES
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivityDataModel
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.ui.createTractActivityIntent
import org.cru.godtools.model.Language
import org.cru.godtools.model.Translation
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.tips.Tip
import org.cru.godtools.tract.PARAM_LIVE_SHARE_STREAM
import org.cru.godtools.tract.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.repository.TranslationsRepository
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

private const val TOOL = "test"

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
@Ignore("AGP 7.3.0-alpha03 broke using Data Binding classes from library unit tests.")
class TractActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    @Inject
    lateinit var dao: GodToolsDao
    @Inject
    lateinit var manifestManager: ManifestManager
    @Inject
    lateinit var translationsRepository: TranslationsRepository

    private lateinit var lottieUtils: MockedStatic<*>

    private fun <R> scenario(
        intent: Intent = context.createTractActivityIntent(TOOL, Locale.ENGLISH),
        block: (ActivityScenario<TractActivity>) -> R
    ) = ActivityScenario.launch<TractActivity>(intent).use(block)
    private fun <R> deepLinkScenario(uri: Uri, block: (ActivityScenario<TractActivity>) -> R) =
        scenario(Intent(Intent.ACTION_VIEW, uri), block)

    @Before
    fun setup() {
        hiltRule.inject()
        every {
            dao.getLiveData(match<Query<Language>> { it.table.type == Language::class.java })
        } returns MutableLiveData(emptyList())
        lottieUtils = mockStatic(Class.forName("org.cru.godtools.tract.util.LottieUtilsKt"))
    }

    @After
    fun cleanup() {
        lottieUtils.closeOnDemand()
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
        deepLinkScenario(Uri.parse("https://knowgod.com/fr/test?primaryLanguage=en&parallelLanguage=es,fr")) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(Locale.ENGLISH, it.dataModel.primaryLocales.value!!.single())
                assertEquals(listOf(Locale("es"), Locale.FRENCH), it.dataModel.parallelLocales.value)
                assertEquals(Locale.FRENCH, it.dataModel.activeLocale.value)
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Deep Link - knowgod_com - With Page Num`() {
        deepLinkScenario(Uri.parse("https://knowgod.com/fr/test/3?primaryLanguage=en&parallelLanguage=es,fr")) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(Locale.ENGLISH, it.dataModel.primaryLocales.value!!.single())
                assertEquals(listOf(Locale("es"), Locale.FRENCH), it.dataModel.parallelLocales.value)
                assertEquals(Locale.FRENCH, it.dataModel.activeLocale.value)
                assertEquals(3, it.initialPage)
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Deep Link - Custom Uri Scheme`() {
        deepLinkScenario(Uri.parse("godtools://org.cru.godtools.test/tool/tract/$TOOL/fr")) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(Locale.FRENCH, it.dataModel.primaryLocales.value!!.single())
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Deep Link - Custom Uri Scheme - Missing Language`() {
        deepLinkScenario(Uri.parse("godtools://org.cru.godtools.test/tool/tract/$TOOL/")) {
            assertEquals(Lifecycle.State.DESTROYED, it.state)
        }
    }

    @Test
    fun `processIntent() - Deep Link - Custom Uri Scheme - With Page Num`() {
        deepLinkScenario(Uri.parse("godtools://org.cru.godtools.test/tool/tract/$TOOL/fr/3")) {
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
                it.dataModel.toolCode.value = "other"
                it.dataModel.primaryLocales.value = listOf(Locale.GERMAN)
                it.dataModel.parallelLocales.value = listOf(Locale.CHINESE)
            }

            // recreate activity, which should keep previously set tool and locales
            it.recreate()
            it.onActivity {
                assertEquals("other", it.dataModel.toolCode.value)
                assertEquals(Locale.GERMAN, it.dataModel.primaryLocales.value!!.single())
                assertEquals(Locale.CHINESE, it.dataModel.parallelLocales.value!!.single())
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Preserve tool and language changes - Deep Link`() {
        deepLinkScenario(Uri.parse("https://knowgod.com/fr/test?primaryLanguage=en&parallelLanguage=es,fr")) {
            // initially parse deep link
            it.onActivity {
                assertEquals("test", it.dataModel.toolCode.value)
                assertEquals(Locale.ENGLISH, it.dataModel.primaryLocales.value!!.single())
                assertEquals(listOf(Locale("es"), Locale.FRENCH), it.dataModel.parallelLocales.value)
                assertFalse(it.isFinishing)
            }

            // update tool & languages
            it.onActivity {
                it.dataModel.toolCode.value = "other"
                it.dataModel.primaryLocales.value = listOf(Locale.GERMAN)
                it.dataModel.parallelLocales.value = listOf(Locale.CHINESE)
            }

            // recreate activity, which should keep previously set tool and locales
            it.recreate()
            it.onActivity {
                assertEquals("other", it.dataModel.toolCode.value)
                assertEquals(Locale.GERMAN, it.dataModel.primaryLocales.value!!.single())
                assertEquals(Locale.CHINESE, it.dataModel.parallelLocales.value!!.single())
                assertFalse(it.isFinishing)
            }
        }
    }
    // endregion Intent Processing

    // region Share Menu Tests
    // region Visibility
    @Test
    @Ignore("The Share menu item was moved to the Settings Dialog")
    fun verifyShareMenuVisible() {
        everyGetTranslation() returns MutableLiveData(Translation())
        whenGetManifest().thenReturn(ImmutableLiveData(Manifest(code = "test", locale = Locale.ENGLISH)))

        scenario {
            it.moveToState(Lifecycle.State.RESUMED)
            it.onActivity {
                with(it.findViewById<Toolbar>(R.id.appbar)!!.menu!!.findItem(R.id.action_share)!!) {
                    assertTrue(isVisible)
                    assertTrue(isEnabled)
                }
            }
        }
    }

    @Test
    @Ignore("The Share menu item was moved to the Settings Dialog")
    fun verifyShareMenuVisibleForDeepLink() {
        everyGetTranslation() returns MutableLiveData(Translation())
        whenGetManifest().thenReturn(ImmutableLiveData(Manifest(code = "test", locale = Locale.ENGLISH)))

        deepLinkScenario(Uri.parse("https://knowgod.com/en/kgp?primaryLanguage=en")) {
            it.moveToState(Lifecycle.State.RESUMED)
            it.onActivity {
                with(it.findViewById<Toolbar>(R.id.appbar)!!.menu!!.findItem(R.id.action_share)!!) {
                    assertTrue(isVisible)
                    assertTrue(isEnabled)
                }
            }
        }
    }

    @Test
    @Ignore("The Share menu item was moved to the Settings Dialog")
    fun verifyShareMenuHiddenWhenNoManifest() {
        everyGetTranslation() returns MutableLiveData(Translation())
        whenGetManifest().thenReturn(ImmutableLiveData(null))

        scenario {
            it.moveToState(Lifecycle.State.RESUMED)
            it.onActivity {
                with(it.findViewById<Toolbar>(R.id.appbar)!!.menu!!.findItem(R.id.action_share)!!) {
                    assertFalse(isVisible)
                    assertFalse(isEnabled)
                }
            }
        }
    }

    @Test
    @Ignore("The Share menu item was moved to the Settings Dialog")
    fun verifyShareMenuHiddenWhenShowingTips() {
        everyGetTranslation() returns MutableLiveData(Translation())
        whenGetManifest()
            .thenReturn(ImmutableLiveData(Manifest(code = "test", locale = Locale.ENGLISH, tips = { listOf(Tip()) })))

        scenario(context.createTractActivityIntent("test", Locale.ENGLISH, showTips = true)) {
            it.moveToState(Lifecycle.State.RESUMED)
            it.onActivity {
                with(it.findViewById<Toolbar>(R.id.appbar)!!.menu!!.findItem(R.id.action_share)!!) {
                    assertFalse(isVisible)
                    assertFalse(isEnabled)
                }
            }
        }
    }

    @Test
    @Ignore("The Share menu item was moved to the Settings Dialog")
    fun verifyShareMenuHiddenWhenLiveShareSubscriber() {
        everyGetTranslation() returns MutableLiveData(Translation())
        whenGetManifest().thenReturn(ImmutableLiveData(Manifest(code = "test", locale = Locale.ENGLISH)))

        deepLinkScenario(Uri.parse("https://knowgod.com/en/kgp?primaryLanguage=en&$PARAM_LIVE_SHARE_STREAM=asdf")) {
            it.moveToState(Lifecycle.State.RESUMED)
            it.onActivity {
                with(it.findViewById<Toolbar>(R.id.appbar)!!.menu!!.findItem(R.id.action_share)!!) {
                    assertFalse(isVisible)
                    assertFalse(isEnabled)
                }
            }
        }
    }
    // endregion Visibility
    // endregion Share Menu Tests

    private val TractActivity.dataModel get() = viewModels<MultiLanguageToolActivityDataModel>().value

    private fun everyGetTranslation(tool: String? = null, locale: Locale? = null) =
        every { translationsRepository.getLatestTranslationLiveData(tool ?: any(), locale ?: any(), any(), any()) }
    private fun whenGetManifest(tool: String = any(), locale: Locale = any()) =
        whenever(manifestManager.getLatestPublishedManifestLiveData(tool, locale))
}
