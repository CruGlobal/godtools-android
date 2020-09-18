package org.cru.godtools.dagger.features

import android.app.Application
import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.cru.godtools.base.Settings
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.GodToolsDao

@EntryPoint
@InstallIn(ApplicationComponent::class)
interface BundledContentFeatureDependencies {
    fun application(): Application
    @ApplicationContext
    fun appContext(): Context
    fun dao(): GodToolsDao
    fun downloadManager(): GodToolsDownloadManager
    fun eventBus(): EventBus
    fun jsonApiConverter(): JsonApiConverter
    fun settings(): Settings
}
