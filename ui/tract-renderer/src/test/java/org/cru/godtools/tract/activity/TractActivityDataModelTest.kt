package org.cru.godtools.tract.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.xml.model.Manifest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.keynote.godtools.android.db.GodToolsDao
import java.util.Locale

private const val TOOL = "kgp"

class TractActivityDataModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dao: GodToolsDao
    private lateinit var manifestManager: ManifestManager
    private lateinit var dataModel: TractActivityDataModel

    private lateinit var observer: Observer<Any?>

    @Before
    fun setupDataModel() {
        dao = mock()
        manifestManager = mock()
        dataModel = TractActivityDataModel(dao, manifestManager, SavedStateHandle())
    }

    @Before
    fun setupObserver() {
        observer = mock()
    }

    @Test
    fun verifyManifests() {
        wheneverGetManifest(TOOL, Locale.ENGLISH).thenReturn(emptyLiveData())
        wheneverGetManifest(TOOL, Locale.FRENCH).thenReturn(MutableLiveData())
        dataModel.tool.value = TOOL
        dataModel.locales.value = listOf(Locale.ENGLISH, Locale.FRENCH)

        dataModel.manifests.observeForever(observer)
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.ENGLISH))
        verify(manifestManager).getLatestPublishedManifestLiveData(any(), eq(Locale.FRENCH))
        argumentCaptor<List<Manifest?>> {
            verify(observer).onChanged(capture())
            assertThat(lastValue, contains(null, null))
        }
    }

    @Test
    fun verifyManifestsNoLocales() {
        dataModel.manifests.observeForever(observer)
        assertThat(dataModel.manifests.value, empty())
        dataModel.tool.value = TOOL
        assertThat(dataModel.manifests.value, empty())
        verify(observer).onChanged(eq(emptyList<Manifest?>()))
    }

    private fun wheneverGetManifest(tool: String, locale: Locale) =
        whenever(manifestManager.getLatestPublishedManifestLiveData(tool, locale))
}
