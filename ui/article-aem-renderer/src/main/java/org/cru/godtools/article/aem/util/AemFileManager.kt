package org.cru.godtools.article.aem.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.cru.godtools.base.FileManager

@Singleton
class AemFileManager @Inject constructor(@ApplicationContext context: Context) : FileManager(context, "aem-resources")
