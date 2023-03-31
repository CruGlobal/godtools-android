package org.cru.godtools.dagger.features

import android.app.Application
import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.LastSyncTimeRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.greenrobot.eventbus.EventBus

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BundledContentFeatureDependencies {
    fun application(): Application
    @ApplicationContext
    fun appContext(): Context
    fun attachmentsRepository(): AttachmentsRepository
    fun downloadManager(): GodToolsDownloadManager
    fun eventBus(): EventBus
    fun jsonApiConverter(): JsonApiConverter
    fun languagesRepository(): LanguagesRepository
    fun lastSyncTimeRepository(): LastSyncTimeRepository
    fun settings(): Settings
    fun toolsRepository(): ToolsRepository
    fun translationsRepository(): TranslationsRepository
}
