package org.cru.godtools.article.aem.util

import android.content.Context
import androidx.annotation.WorkerThread
import java.io.File
import org.jetbrains.annotations.Contract

private val resourcesDir = "aem-resources"

/**
 * Attempts to create the resource directory if it doesn't exist.
 *
 * @param context The context object
 * @return true if the resource directory exists, false if it doesn't exist
 */
@WorkerThread
fun ensureResourcesDirExists(context: Context): Boolean {
    val dir = getResourcesDir(context)
    return (dir.exists() || dir.mkdirs()) && dir.isDirectory
}

fun getResourcesDir(context: Context): File {
    return File(context.filesDir, resourcesDir)
}

@Contract("_,null -> null; _,!null -> !null")
fun getFile(context: Context, name: String?): File? {
    return name?.let { File(getResourcesDir(context), name) }
}

fun createNewFile(context: Context): File {
    return File.createTempFile("aem-", ".bin", getResourcesDir(context))
}
