package org.cru.godtools.article.aem.service

import androidx.room.InvalidationTracker
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import java.security.MessageDigest
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.util.AemFileManager
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.startsWith
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mockStatic

@OptIn(ExperimentalPathApi::class)
class AemArticleManagerTest {
    private val testDir = createTempDirectory().toFile()

    private lateinit var aemDb: ArticleRoomDatabase
    private lateinit var invalidationTracker: InvalidationTracker
    private lateinit var fileManager: AemFileManager

    private lateinit var articleManager: KotlinAemArticleManager

    @Before
    fun setup() {
        invalidationTracker = mock()
        aemDb = mock { on { invalidationTracker } doReturn invalidationTracker }
        fileManager = AemFileManager(mock { on { filesDir } doReturn testDir })

        articleManager = KotlinAemArticleManager(aemDb, fileManager)
    }

    @After
    fun cleanup() {
        testDir.deleteRecursively()
    }

    // region InputStream.writeToDisk()
    @Test
    fun testWriteToDisk() {
        val data = "testWriteToDisk()"
        val file = with(articleManager) { data.byteInputStream().use { it.writeToDisk() } }
        assertNotNull(file)
        assertArrayEquals(data.toByteArray(), file!!.readBytes())
    }

    @Test
    fun testWriteToDiskDedup() {
        assumeNotNull(MessageDigest.getInstance("SHA-1"))
        val data = "testWriteToDiskDedup()"
        val digestName = MessageDigest.getInstance("SHA-1").digest(data.toByteArray())
            .joinToString("", postfix = ".bin") { String.format("%02x", it) }

        val file1 = with(articleManager) { data.byteInputStream().use { it.writeToDisk() } }
        val file2 = with(articleManager) { data.byteInputStream().use { it.writeToDisk() } }

        assertEquals(file1, file2)
        assertEquals(digestName, file1!!.name)
    }

    @Test
    fun testWriteToDiskNoDedupWithoutDigest() {
        mockStatic(MessageDigest::class.java).use {
            it.`when`<MessageDigest?> { MessageDigest.getInstance("SHA-1") } doReturn null

            val data = "testWriteToDiskNoDedupWithoutDigest()"

            val file1 = with(articleManager) { data.byteInputStream().use { it.writeToDisk() } }
            val file2 = with(articleManager) { data.byteInputStream().use { it.writeToDisk() } }

            assertNotEquals(file1, file2)
            assertNotEquals(file1!!.name, file2!!.name)
            assertThat(file1.name, startsWith("aem-"))
            assertThat(file2.name, startsWith("aem-"))
        }
    }
    // endregion InputStream.writeToDisk()

    @Test
    fun testRoundTimestamp() {
        assertEquals(15000, AemArticleManager.roundTimestamp(15234, 1000))
    }
}
