@file:JvmName("FileUtils")

package org.cru.godtools.base.util

import android.content.Context
import java.io.File

// TODO: Context.filesDir accesses storage and is blocking.
val Context.godToolsResourcesDir get() = File(filesDir, "resources")
fun Context.getGodToolsFile(name: String?) = name?.let { File(godToolsResourcesDir, name) }
