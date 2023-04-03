package org.cru.godtools.base.tool.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Translation
import org.cru.godtools.shared.tool.parser.ManifestParser
import org.cru.godtools.shared.tool.parser.ParserResult
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

private const val MANIFEST_NAME = "manifest.xml"

@OptIn(ExperimentalCoroutinesApi::class)
class ManifestManagerTest {
    private val parser: ManifestParser = mockk()
    private val translationsRepository: TranslationsRepository = mockk(relaxUnitFun = true)

    private val manager: ManifestManager = ManifestManager(parser, translationsRepository)

    private val translation = Translation().apply {
        manifestFileName = MANIFEST_NAME
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
        coVerifyAll { translationsRepository.markBrokenManifestNotDownloaded(MANIFEST_NAME) }
    }

    @Test
    fun testGetManifestNotFound() = runTest {
        coEvery { parser.parseManifest(MANIFEST_NAME) } returns mockk<ParserResult.Error.NotFound>()

        val result = manager.getManifest(translation)
        assertNull(result)
        coVerifyAll { translationsRepository.markBrokenManifestNotDownloaded(MANIFEST_NAME) }
    }
}
