package org.cru.godtools.article.aem.util

import android.content.Context
import org.jetbrains.annotations.Contract

@Contract("_,null -> null; _,!null -> !null")
fun getFile(context: Context, name: String?) = name?.let { context.aemFileManager.getFileBlocking(name) }
