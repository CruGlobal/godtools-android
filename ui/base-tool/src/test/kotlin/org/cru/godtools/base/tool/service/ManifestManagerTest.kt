package org.cru.godtools.base.tool.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.randomTranslation
import org.cru.godtools.shared.tool.parser.ManifestParser
import org.cru.godtools.shared.tool.parser.ParserResult
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class ManifestManagerTest {
    private val parser: ManifestParser = mockk()
    private val translationsRepository: TranslationsRepository = mockk(relaxUnitFun = true)

    private val manager: ManifestManager = ManifestManager(parser, translationsRepository)

    private val translation = randomTranslation()

    @Test
    fun `getManifest()`() = runTest {
        val manifest: Manifest = mockk()
        coEvery { parser.parseManifest(translation.manifestFileName!!) } returns ParserResult.Data(manifest)

        val result = manager.getManifest(translation)
        assertSame(manifest, result)
        coVerifyAll {
            parser.parseManifest(translation.manifestFileName!!)
        }
    }

    @Test
    fun `getManifest() - Cache Valid Manifests`() = runTest {
        coEvery { parser.parseManifest(any()) } answers { ParserResult.Data(mockk()) }

        val result1 = manager.getManifest(translation)
        val result2 = manager.getManifest(translation)
        assertSame(result1, result2)
        coVerify(exactly = 1) { parser.parseManifest(translation.manifestFileName!!) }
        confirmVerified(parser)
    }

    @Test
    fun `getManifest() - Corrupted`() = runTest {
        coEvery { parser.parseManifest(any()) } returns mockk<ParserResult.Error.Corrupted>()

        val result = manager.getManifest(translation)
        assertNull(result)
        coVerifyAll { translationsRepository.markBrokenManifestNotDownloaded(translation.manifestFileName!!) }
    }

    @Test
    fun `getManifest() - Not Found`() = runTest {
        coEvery { parser.parseManifest(any()) } returns mockk<ParserResult.Error.NotFound>()

        val result = manager.getManifest(translation)
        assertNull(result)
        coVerifyAll { translationsRepository.markBrokenManifestNotDownloaded(translation.manifestFileName!!) }
    }
}
