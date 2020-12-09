package org.cru.godtools.base

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
import kotlinx.coroutines.withContext

@Singleton
class FileManager @Inject internal constructor(@ApplicationContext private val context: Context) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val resourcesDirTask = coroutineScope.async { File(context.filesDir, "resources") }
    private val resourcesDirCreated =
        coroutineScope.async { resourcesDirTask.await().run { (exists() || mkdirs()) && isDirectory } }

    suspend fun createResourcesDir() = resourcesDirCreated.await()
    suspend fun getResourcesDir() = resourcesDirTask.await()

    suspend fun getFile(filename: String) = File(resourcesDirTask.await(), filename)

    private val resourcesDir by lazy { runBlocking { resourcesDirTask.await() } }
    fun getFileBlocking(filename: String) = File(resourcesDir, filename)

    @Throws(FileNotFoundException::class)
    suspend fun getInputStream(filename: String): InputStream =
        withContext(Dispatchers.IO) { getFile(filename).inputStream() }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface Provider {
        val fileManager: FileManager
    }
}

val Context.fileManager get() = EntryPoints.get(applicationContext, FileManager.Provider::class.java).fileManager
