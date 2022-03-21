package org.cru.godtools.tract.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

private const val TOOL = "test"

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class TractActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context: Context get() = getInstrumentation().context
    @Inject
    lateinit var dao: GodToolsDao
    @Inject
    lateinit var manifestManager: ManifestManager

    private lateinit var lottieUtils: MockedStatic<*>

    @Before
    fun setup() {
        hiltRule.inject()
        dao.stub {
            on {
                getLiveData(argThat<Query<Language>> { table.type == Language::class.java })
            } doReturn ImmutableLiveData(emptyList())
        }
        lottieUtils = mockStatic(Class.forName("org.cru.godtools.tract.util.LottieUtilsKt"))
    }

    @After
    fun cleanup() {
        lottieUtils.closeOnDemand()
    }

    // region Intent Processing
    @Test
    fun `processIntent() - Valid direct`() {
        ActivityScenario.launch<TractActivity>(context.createTractActivityIntent(TOOL, Locale.ENGLISH)).use {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(Locale.ENGLISH, it.dataModel.primaryLocales.value!!.single())
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Valid direct with parallel language`() {
        val intent = context.createTractActivityIntent(TOOL, Locale.ENGLISH, Locale.FRENCH)
        ActivityScenario.launch<TractActivity>(intent).use {
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
        val intent = context.createTractActivityIntent(TOOL, Locale.ENGLISH)
        intent.removeExtra(EXTRA_TOOL)
        ActivityScenario.launch<TractActivity>(intent).use {
            assertEquals(Lifecycle.State.DESTROYED, it.state)
        }
    }

    @Test
    fun `processIntent() - Invalid missing locales`() {
        val intent = context.createTractActivityIntent(TOOL, Locale.ENGLISH)
        intent.removeExtra(EXTRA_LANGUAGES)
        ActivityScenario.launch<TractActivity>(intent).use {
            assertEquals(Lifecycle.State.DESTROYED, it.state)
        }
    }

    @Test
    fun `processIntent() - Valid deeplink`() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://knowgod.com/fr/test?primaryLanguage=en&parallelLanguage=es,fr")
        )
        ActivityScenario.launch<TractActivity>(intent).use {
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
    fun `processIntent() - Preserve tool and language changes - Direct`() {
        val intent = context.createTractActivityIntent(TOOL, Locale.ENGLISH, Locale.FRENCH)
        ActivityScenario.launch<TractActivity>(intent).use {
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
    fun `processIntent() - Preserve tool and language changes - Deeplink`() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://knowgod.com/fr/test?primaryLanguage=en&parallelLanguage=es,fr")
        )
        ActivityScenario.launch<TractActivity>(intent).use {
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
    fun verifyShareMenuVisible() {
        whenGetTranslation().thenReturn(ImmutableLiveData(Translation()))
        whenGetManifest().thenReturn(ImmutableLiveData(Manifest(code = "test", locale = Locale.ENGLISH)))

        ActivityScenario.launch<TractActivity>(context.createTractActivityIntent("test", Locale.ENGLISH)).use {
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
    fun verifyShareMenuVisibleForDeepLink() {
        whenGetTranslation().thenReturn(ImmutableLiveData(Translation()))
        whenGetManifest().thenReturn(ImmutableLiveData(Manifest(code = "test", locale = Locale.ENGLISH)))

        val intent = Intent(context, TractActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("https://knowgod.com/en/kgp?primaryLanguage=en")
        }
        ActivityScenario.launch<TractActivity>(intent).use {
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
    fun verifyShareMenuHiddenWhenNoManifest() {
        whenGetTranslation().thenReturn(ImmutableLiveData(Translation()))
        whenGetManifest().thenReturn(ImmutableLiveData(null))

        ActivityScenario.launch<TractActivity>(context.createTractActivityIntent("test", Locale.ENGLISH)).use {
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
    fun verifyShareMenuHiddenWhenShowingTips() {
        whenGetTranslation().thenReturn(ImmutableLiveData(Translation()))
        whenGetManifest()
            .thenReturn(ImmutableLiveData(Manifest(code = "test", locale = Locale.ENGLISH, tips = { listOf(Tip()) })))

        val intent = context.createTractActivityIntent("test", Locale.ENGLISH, showTips = true)
        ActivityScenario.launch<TractActivity>(intent).use {
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
    fun verifyShareMenuHiddenWhenLiveShareSubscriber() {
        whenGetTranslation().thenReturn(ImmutableLiveData(Translation()))
        whenGetManifest().thenReturn(ImmutableLiveData(Manifest(code = "test", locale = Locale.ENGLISH)))

        val intent = Intent(context, TractActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("https://knowgod.com/en/kgp?primaryLanguage=en&$PARAM_LIVE_SHARE_STREAM=asdf")
        }
        ActivityScenario.launch<TractActivity>(intent).use {
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

    private fun whenGetTranslation(tool: String? = any(), locale: Locale? = any()) =
        whenever(dao.getLatestTranslationLiveData(tool, locale, any(), any(), any()))
    private fun whenGetManifest(tool: String = any(), locale: Locale = any()) =
        whenever(manifestManager.getLatestPublishedManifestLiveData(tool, locale))
}
