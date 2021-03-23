package org.cru.godtools.article.aem.service

import android.net.Uri
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import androidx.room.InvalidationTracker
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Date
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.ccci.gto.android.common.base.TimeConstants.HOUR_IN_MS
import org.ccci.gto.android.common.base.TimeConstants.MIN_IN_MS
import org.ccci.gto.android.common.kotlin.coroutines.ReadWriteMutex
import org.cru.godtools.article.aem.api.AemApi
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.article.aem.util.AemFileManager
import timber.log.Timber

private const val TAG = "AemArticleManager"

@VisibleForTesting
internal const val CLEANUP_DELAY = HOUR_IN_MS
@VisibleForTesting
internal const val CLEANUP_DELAY_INITIAL = MIN_IN_MS

open class KotlinAemArticleManager @JvmOverloads constructor(
    private val aemDb: ArticleRoomDatabase,
    private val api: AemApi,
    private val fileManager: AemFileManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val filesystemMutex = ReadWriteMutex()

    // region Download Resource
    @WorkerThread
    fun downloadResource(uri: Uri, force: Boolean) {
        runBlocking {
            // short-circuit if the resource doesn't exist or it doesn't need to be downloaded
            val resourceDao = aemDb.resourceDao()
            val resource = resourceDao.find(uri)
            if (resource == null || (!force && !resource.needsDownload())) return@runBlocking

            // download the resource
            try {
                api.downloadResource(uri).takeIf { it.code() == 200 }?.body()?.let { response ->
                    response.byteStream().use {
                        it.writeToDisk { file ->
                            resourceDao.updateLocalFile(uri, response.contentType(), file.name, Date())
                        }
                    }
                }
            } catch (e: IOException) {
                Timber.tag(TAG).d(e, "Error downloading attachment %s", uri)
            }
        }
    }

    @VisibleForTesting
    @Throws(IOException::class)
    internal suspend fun InputStream.writeToDisk(onSuccess: (File) -> Unit): File? {
        if (!fileManager.createDir()) return null

        // create a MessageDigest to dedup files
        val digest = try {
            MessageDigest.getInstance("SHA-1")
        } catch (e: NoSuchAlgorithmException) {
            Timber.tag(TAG).d(e, "Unable to create MessageDigest to dedup AEM resources")
            null
        }

        // lock the file system for writing this resource
        filesystemMutex.read.withLock {
            // write the stream to a temporary file
            val tmpFile = fileManager.createTmpFile("aem-").apply {
                (if (digest != null) DigestOutputStream(outputStream(), digest) else outputStream())
                    .use { copyTo(it) }
            }

            // rename temporary file based on digest
            val dedup = digest?.let { fileManager.getFile("${it.digest().toHexString()}.bin") }
            return when {
                dedup == null -> tmpFile
                dedup.exists() -> {
                    tmpFile.delete()
                    dedup
                }
                tmpFile.renameTo(dedup) -> dedup
                else -> {
                    Timber.tag(TAG).d("cannot rename tmp file %s to %s", tmpFile, dedup)
                    tmpFile
                }
            }.also(onSuccess)
        }
    }
    // endregion Download Resource

    // region Cleanup
    private object RunCleanup

    @OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
    private val cleanupActor = coroutineScope.actor<RunCleanup>(capacity = Channel.CONFLATED) {
        withTimeoutOrNull(CLEANUP_DELAY_INITIAL) { channel.receiveOrNull() }
        while (!channel.isClosedForReceive) {
            cleanOrphanedFiles()
            withTimeoutOrNull(CLEANUP_DELAY) { channel.receiveOrNull() }
        }
    }

    @WorkerThread
    private suspend fun cleanOrphanedFiles() {
        if (!fileManager.createDir()) return

        // lock the filesystem before removing any orphaned files
        filesystemMutex.write.withLock {
            // determine which files are still being referenced
            val valid = aemDb.resourceDao().getAll()
                .mapNotNullTo(mutableSetOf()) { it.getLocalFile(fileManager) }

            // delete any files not referenced
            fileManager.getDir().listFiles()
                ?.filterNot { it in valid }
                ?.forEach { it.delete() }
        }
    }

    init {
        aemDb.invalidationTracker.addObserver(object : InvalidationTracker.Observer(Resource.TABLE_NAME) {
            override fun onInvalidated(tables: Set<String>) {
                if (Resource.TABLE_NAME in tables) cleanupActor.offer(RunCleanup)
            }
        })
    }
    // endregion Cleanup

    @RestrictTo(RestrictTo.Scope.TESTS)
    internal suspend fun shutdown() {
        cleanupActor.close()
        val job = coroutineScope.coroutineContext[Job]
        if (job is CompletableJob) job.complete()
        job?.join()
    }
}

private fun ByteArray.toHexString() = joinToString("") { String.format("%02x", it) }
