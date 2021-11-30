package org.cru.godtools.article.aem.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.cru.godtools.base.FileSystem

@Singleton
class AemFileSystem @Inject constructor(@ApplicationContext context: Context) : FileSystem(context, "aem-resources")
