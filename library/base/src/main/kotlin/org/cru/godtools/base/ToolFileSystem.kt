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
class ToolFileSystem @Inject internal constructor(@ApplicationContext context: Context) :
    FileSystem(context, "resources") {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface Provider {
        val fileSystem: ToolFileSystem
    }
}

val Context.toolFileSystem
    get() = EntryPoints.get(applicationContext, ToolFileSystem.Provider::class.java).fileSystem
