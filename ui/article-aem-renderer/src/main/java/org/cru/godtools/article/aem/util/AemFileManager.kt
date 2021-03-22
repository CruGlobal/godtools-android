package org.cru.godtools.article.aem.util

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import org.cru.godtools.base.FileManager

@Singleton
class AemFileManager @Inject constructor(@ApplicationContext context: Context) : FileManager(context, "aem-resources") {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface Provider {
        val aemFileManager: AemFileManager
    }
}

internal val Context.aemFileManager
    get() = EntryPoints.get(applicationContext, AemFileManager.Provider::class.java).aemFileManager
