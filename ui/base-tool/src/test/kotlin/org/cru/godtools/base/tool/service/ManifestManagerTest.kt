package org.cru.godtools.base.tool.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.cru.godtools.model.Translation
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.service.ManifestParser
import org.cru.godtools.tool.service.ParserResult
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoMoreInteractions

private const val MANIFEST_NAME = "manifest.xml"

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ManifestManagerTest {
    private lateinit var dao: GodToolsDao
    private lateinit var parser: ManifestParser
    private lateinit var manager: ManifestManager

    private lateinit var translation: Translation

    @Before
    fun setup() {
        dao = mock()
        parser = mock()
        manager = ManifestManager(dao, mock(), parser)

        translation = mock {
            on { manifestFileName } doReturn MANIFEST_NAME
        }
    }

    @Test
    fun testGetManifest() {
        val manifest = mock<Manifest>()
        parser.stub {
            onBlocking { parseManifest(MANIFEST_NAME) } doReturn ParserResult.Data(manifest)
        }

        val result = runBlocking { manager.getManifest(translation) }
        assertSame(manifest, result)
    }

    @Test
    fun testGetManifestCacheValidManifests() {
        parser.stub {
            onBlocking { parseManifest(MANIFEST_NAME) } doAnswer { ParserResult.Data(mock()) }
        }

        val result1 = runBlocking { manager.getManifest(translation) }
        val result2 = runBlocking { manager.getManifest(translation) }
        assertSame(result1, result2)
        verifyBlocking(parser) { parseManifest(any()) }
        verifyNoMoreInteractions(parser)
    }

    @Test
    fun testGetManifestCorrupted() {
        parser.stub {
            onBlocking { parseManifest(MANIFEST_NAME) } doReturn mock<ParserResult.Error.Corrupted>()
        }

        val result = runBlocking { manager.getManifest(translation) }
        assertNull(result)
        verifyMarkTranslationAsNotDownloaded()
        verifyNoMoreInteractions(dao)
    }

    @Test
    fun testGetManifestNotFound() {
        parser.stub {
            onBlocking { parseManifest(MANIFEST_NAME) } doReturn mock<ParserResult.Error.NotFound>()
        }

        val result = runBlocking { manager.getManifest(translation) }
        assertNull(result)
        verifyMarkTranslationAsNotDownloaded()
        verifyNoMoreInteractions(dao)
    }

    private fun verifyMarkTranslationAsNotDownloaded() {
        verify(dao).update(
            argThat<Translation> { !isDownloaded },
            eq(TranslationTable.FIELD_MANIFEST.eq(MANIFEST_NAME)),
            eq(TranslationTable.COLUMN_DOWNLOADED)
        )
    }
}
