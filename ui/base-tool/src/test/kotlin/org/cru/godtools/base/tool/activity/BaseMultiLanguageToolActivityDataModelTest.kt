package org.cru.godtools.base.tool.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.Locale
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.model.Translation
import org.cru.godtools.tool.model.Manifest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.aMapWithSize
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anEmptyMap
import org.hamcrest.Matchers.hasEntry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.keynote.godtools.android.db.GodToolsDao
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private const val TOOL = "kgp"

class BaseMultiLanguageToolActivityDataModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // region Objects & Mocks
    private lateinit var dao: GodToolsDao
    private lateinit var manifestManager: ManifestManager
    private lateinit var dataModel: BaseMultiLanguageToolActivityDataModel

    private lateinit var observer: Observer<Any?>

    @Before
    fun setupDataModel() {
        dao = mock()
        manifestManager = mock()
        dataModel = BaseMultiLanguageToolActivityDataModel(dao, manifestManager)
    }

    @Before
    fun setupObserver() {
        observer = mock()
    }

    private fun wheneverGetManifest(tool: String, locale: Locale) =
        whenever(manifestManager.getLatestPublishedManifestLiveData(tool, locale))
    private fun wheneverGetTranslation(tool: String, locale: Locale) =
        whenever(dao.getLatestTranslationLiveData(tool, locale, trackAccess = true))
    // endregion Objects & Mocks

    // region Resolved Data
    // region Property: translations
    @Test
    fun `Property translations`() {
        val translation = Translation()
        wheneverGetTranslation(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetTranslation(TOOL, Locale.FRENCH).thenReturn(MutableLiveData())
        wheneverGetTranslation(TOOL, Locale.CHINESE).thenReturn(MutableLiveData(translation))
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH, Locale.CHINESE)

        dataModel.translations.observeForever(observer)
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.ENGLISH), any(), any(), any())
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.FRENCH), any(), any(), any())
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.CHINESE), any(), any(), any())
        argumentCaptor<Map<Locale, Translation?>> {
            verify(observer).onChanged(capture())
            assertThat(
                lastValue,
                allOf(
                    aMapWithSize(3),
                    hasEntry<Locale, Translation?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Translation?>(Locale.FRENCH, null),
                    hasEntry(Locale.CHINESE, translation)
                )
            )
        }
    }

    @Test
    fun `Property translations - No Locales`() {
        dataModel.translations.observeForever(observer)
        assertThat(dataModel.translations.value, anEmptyMap())
        dataModel.toolCode.value = TOOL
        assertThat(dataModel.translations.value, anEmptyMap())
        verify(observer).onChanged(eq(emptyMap<Locale, Translation?>()))
    }

    @Test
    fun `Property translations - Update Translation`() {
        val french = MutableLiveData<Translation?>(null)
        wheneverGetTranslation(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetTranslation(TOOL, Locale.FRENCH).thenReturn(french)
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.translations.observeForever(observer)
        french.value = Translation()
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.ENGLISH), any(), any(), any())
        verify(dao).getLatestTranslationLiveData(any(), eq(Locale.FRENCH), any(), any(), any())
        argumentCaptor<Map<Locale, Translation?>> {
            verify(observer, times(2)).onChanged(capture())
            assertThat(
                firstValue,
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Translation?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Translation?>(Locale.FRENCH, null)
                )
            )
            assertThat(
                lastValue,
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Translation?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Translation?>(Locale.FRENCH, french.value)
                )
            )
        }
    }
    // endregion Property: translations


    // region Property: manifests
    @Test
    fun `Property manifests`() {
        wheneverGetManifest(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(MutableLiveData())
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.manifests.observeForever(observer)
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.ENGLISH))
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.FRENCH))
        argumentCaptor<Map<Locale, Manifest?>> {
            verify(observer).onChanged(capture())
            assertThat(
                lastValue,
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Manifest?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Manifest?>(Locale.FRENCH, null)
                )
            )
        }
    }

    @Test
    fun `Property manifests - No Locales`() {
        dataModel.manifests.observeForever(observer)
        assertThat(dataModel.manifests.value, anEmptyMap())
        dataModel.toolCode.value = TOOL
        assertThat(dataModel.manifests.value, anEmptyMap())
        verify(observer).onChanged(eq(emptyMap<Locale, Manifest?>()))
    }

    @Test
    fun `Property manifests - Update Locales`() {
        val french = MutableLiveData(Manifest())
        wheneverGetManifest(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(french)
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.manifests.observeForever(observer)
        dataModel.primaryLocales.value = listOf(Locale.FRENCH)
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.ENGLISH))
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.FRENCH))
        argumentCaptor<Map<Locale, Manifest?>> {
            verify(observer, times(2)).onChanged(capture())
            assertThat(
                firstValue,
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Manifest?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Manifest?>(Locale.FRENCH, french.value)
                )
            )
            assertThat(lastValue, allOf(aMapWithSize(1), hasEntry<Locale, Manifest?>(Locale.FRENCH, french.value)))
        }
    }

    @Test
    fun `Property manifests - Update Manifest`() {
        val french = MutableLiveData<Manifest?>()
        wheneverGetManifest(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(french)
        dataModel.toolCode.value = TOOL
        dataModel.primaryLocales.value = listOf(Locale.ENGLISH, Locale.FRENCH)
        dataModel.manifests.observeForever(observer)
        french.value = Manifest()

        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.ENGLISH))
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.FRENCH))
        argumentCaptor<Map<Locale, Manifest?>> {
            verify(observer, times(2)).onChanged(capture())
            assertThat(
                firstValue,
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Manifest?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Manifest?>(Locale.FRENCH, null)
                )
            )
            assertThat(
                lastValue,
                allOf(
                    aMapWithSize(2),
                    hasEntry<Locale, Manifest?>(Locale.ENGLISH, null),
                    hasEntry<Locale, Manifest?>(Locale.FRENCH, french.value)
                )
            )
        }
    }
    // endregion Property: manifests
    // endregion Resolved Data
}
