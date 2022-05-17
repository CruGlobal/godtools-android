package org.cru.godtools.base.tool.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Translation
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.service.ManifestParser
import org.cru.godtools.tool.service.ParserResult
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

private const val MANIFEST_NAME = "manifest.xml"

@OptIn(ExperimentalCoroutinesApi::class)
class ManifestManagerTest {
    private lateinit var dao: GodToolsDao
    private lateinit var parser: ManifestParser
    private lateinit var manager: ManifestManager

    private lateinit var translation: Translation

    @Before
    fun setup() {
        dao = mock()
        parser = mockk()
        manager = ManifestManager(dao, parser)

        translation = mock {
            on { manifestFileName } doReturn MANIFEST_NAME
        }
    }

    @Test
    fun testGetManifest() = runTest {
        val manifest = mockk<Manifest>()
        coEvery { parser.parseManifest(MANIFEST_NAME) } returns ParserResult.Data(manifest)

        val result = manager.getManifest(translation)
        assertSame(manifest, result)
    }

    @Test
    fun testGetManifestCacheValidManifests() = runTest {
        coEvery { parser.parseManifest(any()) } answers { ParserResult.Data(mockk()) }

        val result1 = manager.getManifest(translation)
        val result2 = manager.getManifest(translation)
        assertSame(result1, result2)
        coVerify(exactly = 1) { parser.parseManifest(MANIFEST_NAME) }
        confirmVerified(parser)
    }

    @Test
    fun testGetManifestCorrupted() = runTest {
        coEvery { parser.parseManifest(MANIFEST_NAME) } returns mockk<ParserResult.Error.Corrupted>()

        val result = manager.getManifest(translation)
        assertNull(result)
        verifyMarkTranslationAsNotDownloaded()
        verifyNoMoreInteractions(dao)
    }

    @Test
    fun testGetManifestNotFound() = runTest {
        coEvery { parser.parseManifest(MANIFEST_NAME) } returns mockk<ParserResult.Error.NotFound>()

        val result = manager.getManifest(translation)
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
