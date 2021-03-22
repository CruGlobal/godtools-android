package org.cru.godtools.article.aem.util

import android.content.Context
import androidx.annotation.WorkerThread
import java.io.File
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.Contract

/**
 * Attempts to create the resource directory if it doesn't exist.
 *
 * @param context The context object
 * @return true if the resource directory exists, false if it doesn't exist
 */
@WorkerThread
fun ensureResourcesDirExists(context: Context) = runBlocking { context.aemFileManager.createDir() }

fun getResourcesDir(context: Context) = runBlocking { context.aemFileManager.getDir() }

@Contract("_,null -> null; _,!null -> !null")
fun getFile(context: Context, name: String?) = name?.let { context.aemFileManager.getFileBlocking(name) }

fun createNewFile(context: Context): File {
    return File.createTempFile("aem-", ".bin", getResourcesDir(context))
}
