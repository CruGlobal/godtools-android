package org.cru.godtools.article.aem.service

import androidx.annotation.WorkerThread
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlinx.coroutines.runBlocking
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.util.AemFileManager

private const val TAG = "AemArticleManager"

open class KotlinAemArticleManager(
    private val aemDb: ArticleRoomDatabase,
    private val fileManager: AemFileManager
) {
    companion object {
        @JvmField
        val LOCK_FILESYSTEM: ReadWriteLock = ReentrantReadWriteLock()
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
