package org.cru.godtools.base

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolFileManager @Inject internal constructor(@ApplicationContext context: Context) :
    FileManager(context, "resources") {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface Provider {
        val fileManager: ToolFileManager
    }
}

val Context.toolFileManager
    get() = EntryPoints.get(applicationContext, ToolFileManager.Provider::class.java).fileManager
