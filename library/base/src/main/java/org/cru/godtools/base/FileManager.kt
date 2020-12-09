package org.cru.godtools.base

import android.content.Context
import androidx.annotation.WorkerThread
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

@Singleton
class FileManager @Inject internal constructor(@ApplicationContext private val context: Context) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val resourcesDir = coroutineScope.async { File(context.filesDir, "resources") }
    private val resourcesDirCreated =
        coroutineScope.async { resourcesDir.await().run { (exists() || mkdirs()) && isDirectory } }

    suspend fun createResourcesDir() = resourcesDirCreated.await()
    suspend fun getResourcesDir() = resourcesDir.await()

    suspend fun getFile(filename: String) = File(resourcesDir.await(), filename)

    @WorkerThread
    @Throws(FileNotFoundException::class)
    fun getInputStream(filename: String): InputStream = runBlocking { getFile(filename).inputStream() }
}
