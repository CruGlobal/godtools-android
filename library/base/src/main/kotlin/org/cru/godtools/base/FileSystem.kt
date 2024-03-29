package org.cru.godtools.base

import android.content.Context
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

open class FileSystem(context: Context, dirName: String) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    protected val dirTask = coroutineScope.async { File(context.filesDir, dirName) }
    private val dirCreated = coroutineScope.async { dirTask.await().run { (exists() || mkdirs()) && isDirectory } }

    suspend fun exists() = dirCreated.await()
    suspend fun rootDir() = dirTask.await()

    suspend fun file(filename: String) = File(dirTask.await(), filename)

    suspend fun openInputStream(filename: String): InputStream =
        withContext(Dispatchers.IO) { file(filename).inputStream() }

    private val dir by lazy { runBlocking { dirTask.await() } }
    fun getFileBlocking(filename: String) = File(dir, filename)
}
