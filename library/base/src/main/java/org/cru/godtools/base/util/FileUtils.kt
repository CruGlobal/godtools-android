@file:JvmName("FileUtils")

package org.cru.godtools.base.util

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.cru.godtools.base.fileManager

@Deprecated("use FileManager to access files instead")
fun Context.getGodToolsFile(name: String?) = name?.let { runBlocking { fileManager.getFile(name) } }
