package org.cru.godtools.base

import android.content.Context
import androidx.annotation.WorkerThread
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileNotFoundException
import java.io.InputStream
import javax.inject.Inject
import org.cru.godtools.base.util.getGodToolsFile

@Reusable
class FileManager @Inject internal constructor(@ApplicationContext private val context: Context) {
    fun getFile(filename: String?) = context.getGodToolsFile(filename)

    @WorkerThread
    @Throws(FileNotFoundException::class)
    fun getInputStream(filename: String?): InputStream? = getFile(filename)?.inputStream()
}
