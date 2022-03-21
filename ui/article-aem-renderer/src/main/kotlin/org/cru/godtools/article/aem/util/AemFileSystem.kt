package org.cru.godtools.article.aem.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cru.godtools.base.FileSystem

@Singleton
class AemFileSystem @Inject constructor(@ApplicationContext context: Context) : FileSystem(context, "aem-resources") {
    suspend fun createTmpFile(prefix: String, suffix: String? = null): File =
        withContext(Dispatchers.IO) { File.createTempFile(prefix, suffix, dirTask.await()) }
}
