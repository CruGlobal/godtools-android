package org.cru.godtools.tract.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import java.util.Locale
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Translation
import org.cru.godtools.tool.model.Manifest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.keynote.godtools.android.db.GodToolsDao
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private const val TOOL = "kgp"

class TractActivityDataModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dao: GodToolsDao
    private lateinit var downloadManager: GodToolsDownloadManager
    private lateinit var manifestManager: ManifestManager
    private lateinit var dataModel: TractActivityDataModel

    private val isConnnected = MutableLiveData(true)
    private lateinit var observer: Observer<Any?>

    @Before
    fun setupDataModel() {
        dao = mock()
        downloadManager = mock()
        manifestManager = mock()
        dataModel = TractActivityDataModel(dao, downloadManager, manifestManager, isConnnected, SavedStateHandle())
            .apply { isInitialSyncFinished.value = true }
    }

    @Before
    fun setupObserver() {
        observer = mock()
    }

    // region Property: visibleLocales
    @Test
    fun verifyVisibleLocalesFirstPrimaryDownloaded() {
        // setup test
        wheneverGetTranslation(TOOL, Locale.FRENCH).thenReturn(MutableLiveData(Translation()))
        wheneverGetTranslation(TOOL, Locale.GERMAN).thenReturn(MutableLiveData())
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(MutableLiveData(Manifest()))
        wheneverGetManifest(TOOL, Locale.GERMAN).thenReturn(MutableLiveData())
        dataModel.toolCode.value = TOOL
        dataModel.setActiveLocale(Locale.FRENCH)
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.visibleLocales.observeForever(observer)

        // run logic and verify results
        assertThat(
            "first language should be visible because it is downloaded",
            dataModel.visibleLocales.value, contains(Locale.FRENCH)
        )
    }

    @Test
    fun verifyVisibleLocalesFirstPrimaryLoadingAndActiveSecondPrimaryDownloaded() {
        // setup test
        wheneverGetTranslation(TOOL, Locale.FRENCH).thenReturn(MutableLiveData(Translation()))
        wheneverGetTranslation(TOOL, Locale.GERMAN).thenReturn(MutableLiveData(Translation()))
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(MutableLiveData())
        wheneverGetManifest(TOOL, Locale.GERMAN).thenReturn(MutableLiveData(Manifest()))
        dataModel.toolCode.value = TOOL
        dataModel.setActiveLocale(Locale.FRENCH)
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.visibleLocales.observeForever(observer)

        // run logic and verify results
        assertThat(
            "french is available because it is currently active and potentially available",
            dataModel.availableLocales.value, contains(Locale.FRENCH)
        )
        assertThat(
            "neither language should be visible because the preferred primary is still loading",
            dataModel.visibleLocales.value, empty()
        )
    }

    @Test
    fun verifyVisibleLocalesFirstPrimaryMissingSecondPrimaryLoadingNeitherActive() {
        // setup test
        wheneverGetTranslation(TOOL, Locale.FRENCH).thenReturn(MutableLiveData(null))
        wheneverGetTranslation(TOOL, Locale.GERMAN).thenReturn(MutableLiveData(Translation()))
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(MutableLiveData(null))
        wheneverGetManifest(TOOL, Locale.GERMAN).thenReturn(MutableLiveData())
        dataModel.toolCode.value = TOOL
        dataModel.setActiveLocale(Locale.ENGLISH)
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.visibleLocales.observeForever(observer)

        // run logic and verify results
        assertThat(
            "german should be available because it is potentially available",
            dataModel.availableLocales.value, contains(Locale.GERMAN)
        )
        assertThat(
            "neither language should be visible because the primary is still loading",
            dataModel.visibleLocales.value, empty()
        )
    }

    @Test
    fun verifyVisibleLocalesFirstPrimaryLoadedSecondPrimaryLoadingAndActive() {
        // setup test
        wheneverGetTranslation(TOOL, Locale.FRENCH).thenReturn(MutableLiveData(Translation()))
        wheneverGetTranslation(TOOL, Locale.GERMAN).thenReturn(MutableLiveData(Translation()))
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(MutableLiveData(Manifest()))
        wheneverGetManifest(TOOL, Locale.GERMAN).thenReturn(MutableLiveData())
        dataModel.toolCode.value = TOOL
        dataModel.setActiveLocale(Locale.GERMAN)
        dataModel.primaryLocales.value = listOf(Locale.FRENCH, Locale.GERMAN)
        dataModel.visibleLocales.observeForever(observer)

        // run logic and verify results
        assertThat(
            "german should be available because it is potentially available",
            dataModel.availableLocales.value, contains(Locale.GERMAN)
        )
        assertThat(
            "neither language should be visible because the primary is still loading",
            dataModel.visibleLocales.value, empty()
        )
    }
    // endregion Property: visibleLocales

    private fun wheneverGetManifest(tool: String, locale: Locale) =
        whenever(manifestManager.getLatestPublishedManifestLiveData(tool, locale))

    private fun wheneverGetTranslation(tool: String, locale: Locale) =
        whenever(dao.getLatestTranslationLiveData(tool, locale, trackAccess = true))
}
