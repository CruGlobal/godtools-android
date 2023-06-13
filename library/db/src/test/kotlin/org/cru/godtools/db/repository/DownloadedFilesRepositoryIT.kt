package org.cru.godtools.db.repository

import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.DownloadedFile
import org.cru.godtools.model.DownloadedTranslationFile
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class DownloadedFilesRepositoryIT {
    protected val testScope = TestScope()
    abstract val repository: DownloadedFilesRepository

    private val file1 = DownloadedFile("file1.ext")
    private val file2 = DownloadedFile("file2.ext")
    private val translation1 = DownloadedTranslationFile(1, "file1.ext")

    @Test
    fun `findDownloadedFile() & insertOrIgnore() & delete()`() = testScope.runTest {
        assertNull(repository.findDownloadedFile(file1.filename))

        repository.insertOrIgnore(file1)
        assertEquals(file1, repository.findDownloadedFile(file1.filename))

        repository.delete(file1)
        assertNull(repository.findDownloadedFile(file1.filename))
    }

    @Test
    fun `getDownloadedFiles()`() = testScope.runTest {
        assertThat(repository.getDownloadedFiles(), empty())

        repository.insertOrIgnore(file1)
        repository.insertOrIgnore(file2)
        assertThat(repository.getDownloadedFiles(), containsInAnyOrder(file1, file2))
    }

    @Test
    fun `getDownloadedFilesFlow()`() = testScope.runTest {
        repository.getDownloadedFilesFlow().test {
            assertThat(awaitItem(), empty())

            repository.insertOrIgnore(file1)
            repository.insertOrIgnore(file2)
            runCurrent()
            assertThat(expectMostRecentItem(), containsInAnyOrder(file1, file2))
        }
    }

    @Test
    open fun `getDownloadedTranslationFiles() & insertOrIgnore() & delete()`() = testScope.runTest {
        assertThat(repository.getDownloadedTranslationFiles(), empty())

        repository.insertOrIgnore(translation1)
        assertThat(repository.getDownloadedTranslationFiles(), containsInAnyOrder(translation1))

        repository.delete(translation1)
        assertThat(repository.getDownloadedTranslationFiles(), empty())
    }
}
