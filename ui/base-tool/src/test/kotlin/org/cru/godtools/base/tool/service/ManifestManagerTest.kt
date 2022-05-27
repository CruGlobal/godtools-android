package org.cru.godtools.base.tool.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
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

private const val MANIFEST_NAME = "manifest.xml"

@OptIn(ExperimentalCoroutinesApi::class)
class ManifestManagerTest {
    private val dao: GodToolsDao = mockk(relaxed = true)
    private val parser: ManifestParser = mockk()
    private lateinit var manager: ManifestManager

    private val translation = Translation().apply {
        manifestFileName = MANIFEST_NAME
    }

    @Before
    fun setup() {
        manager = ManifestManager(dao, parser)
    }

    @Test
    fun testGetManifest() = runTest {
        val manifest: Manifest = mockk()
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
        confirmVerified(dao)
    }

    @Test
    fun testGetManifestNotFound() = runTest {
        coEvery { parser.parseManifest(MANIFEST_NAME) } returns mockk<ParserResult.Error.NotFound>()

        val result = manager.getManifest(translation)
        assertNull(result)
        verifyMarkTranslationAsNotDownloaded()
        confirmVerified(dao)
    }

    private fun verifyMarkTranslationAsNotDownloaded() = verify {
        dao.update(
            match<Translation> { !it.isDownloaded },
            TranslationTable.FIELD_MANIFEST.eq(MANIFEST_NAME),
            TranslationTable.COLUMN_DOWNLOADED
        )
    }
}
