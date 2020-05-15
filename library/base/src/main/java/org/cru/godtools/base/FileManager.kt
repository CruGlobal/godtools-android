package org.cru.godtools.base

import android.content.Context
import androidx.annotation.WorkerThread
import dagger.Reusable
import org.cru.godtools.base.util.getGodToolsFile
import java.io.FileNotFoundException
import java.io.InputStream
import javax.inject.Inject

@Reusable
class FileManager @Inject internal constructor(private val context: Context) {
    fun getFile(filename: String?) = context.getGodToolsFile(filename)

    @WorkerThread
    @Throws(FileNotFoundException::class)
    fun getInputStream(filename: String?): InputStream? = getFile(filename)?.inputStream()
}
