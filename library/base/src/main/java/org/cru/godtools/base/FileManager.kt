package org.cru.godtools.base

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileNotFoundException
import java.io.InputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cru.godtools.base.util.createGodToolsResourcesDir
import org.cru.godtools.base.util.getGodToolsFile

@Reusable
class FileManager @Inject internal constructor(@ApplicationContext private val context: Context) {
    @AnyThread
    suspend fun createResourcesDir() = withContext(Dispatchers.IO) { context.createGodToolsResourcesDir() }
    fun getFile(filename: String?) = context.getGodToolsFile(filename)

    @WorkerThread
    @Throws(FileNotFoundException::class)
    fun getInputStream(filename: String?): InputStream? = getFile(filename)?.inputStream()
}
