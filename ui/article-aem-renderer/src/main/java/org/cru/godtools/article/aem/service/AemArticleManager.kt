package org.cru.godtools.article.aem.service

import androidx.annotation.WorkerThread
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlinx.coroutines.runBlocking
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.util.AemFileManager
import timber.log.Timber

private const val TAG = "AemArticleManager"

open class KotlinAemArticleManager(
    private val aemDb: ArticleRoomDatabase,
    private val fileManager: AemFileManager
) {
    companion object {
        @JvmField
        val LOCK_FILESYSTEM: ReadWriteLock = ReentrantReadWriteLock()
    }

    @Throws(IOException::class)
    fun InputStream.writeToDisk(): File? {
        if (!runBlocking { fileManager.createDir() }) return null

        // create a MessageDigest to dedup files
        val digest = try {
            MessageDigest.getInstance("SHA-1")
        } catch (e: NoSuchAlgorithmException) {
            Timber.tag(TAG).d(e, "Unable to create MessageDigest to dedup AEM resources")
            null
        }

        // lock the file system for writing this resource
        val lock = LOCK_FILESYSTEM.readLock()
        return try {
            lock.lock()

            runBlocking {
                // write the stream to a temporary file
                val tmpFile = fileManager.createTmpFile("aem-").apply {
                    (if (digest != null) DigestOutputStream(outputStream(), digest) else outputStream())
                        .use { copyTo(it) }
                }

                // rename temporary file based on digest
                if (digest == null) return@runBlocking tmpFile
                val file = fileManager.getFile("${digest.digest().toHexString()}.bin")
                return@runBlocking when {
                    file.exists() -> {
                        tmpFile.delete()
                        file
                    }
                    tmpFile.renameTo(file) -> file
                    else -> {
                        Timber.tag(TAG).d("cannot rename tmp file %s to %s", tmpFile, file)
                        tmpFile
                    }
                }
            }
        } finally {
            lock.unlock()
        }
    }

    @WorkerThread
    protected fun cleanOrphanedFiles() {
        if (!runBlocking { fileManager.createDir() }) return

        // lock the filesystem before removing any orphaned files
        val lock = LOCK_FILESYSTEM.writeLock()
        try {
            lock.lock()

            runBlocking {
                // determine which files are still being referenced
                val valid = aemDb.resourceDao().getAll()
                    .mapNotNullTo(mutableSetOf()) { it.getLocalFile(fileManager) }

                // delete any files not referenced
                fileManager.getDir().listFiles()
                    ?.filterNot { it in valid }
                    ?.forEach { it.delete() }
            }
        } finally {
            lock.unlock()
        }
    }
}

private fun ByteArray.toHexString() = joinToString("") { String.format("%02x", it) }
