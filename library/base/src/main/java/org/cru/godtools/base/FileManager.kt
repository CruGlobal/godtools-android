package org.cru.godtools.base

import android.content.Context
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

abstract class FileManager(protected val context: Context, private val dirName: String) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val dirTask = coroutineScope.async { File(context.filesDir, dirName) }
    private val dirCreated = coroutineScope.async { dirTask.await().run { (exists() || mkdirs()) && isDirectory } }

    suspend fun createDir() = dirCreated.await()
    suspend fun getDir() = dirTask.await()

    suspend fun createTmpFile(prefix: String, suffix: String? = null): File =
        withContext(Dispatchers.IO) { File.createTempFile(prefix, suffix, dirTask.await()) }
    suspend fun getFile(filename: String) = File(dirTask.await(), filename)

    @Throws(FileNotFoundException::class)
    suspend fun getInputStream(filename: String): InputStream =
        withContext(Dispatchers.IO) { getFile(filename).inputStream() }

    private val dir by lazy { runBlocking { dirTask.await() } }
    fun getFileBlocking(filename: String) = File(dir, filename)
}
