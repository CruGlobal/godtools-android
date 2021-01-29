package org.cru.godtools.ui.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.orEmpty
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.findLiveData
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.base.Settings
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Language
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

@HiltViewModel
class ToolsAdapterViewModel @Inject constructor(
    private val dao: GodToolsDao,
    private val downloadManager: GodToolsDownloadManager,
    private val settings: Settings
) : ViewModel() {
    private val toolViewModels = mutableMapOf<String, ToolViewModel>()
    fun getToolViewModel(tool: String) = toolViewModels.getOrPut(tool) { ToolViewModel(tool) }

    inner class ToolViewModel(private val tool: String) {
        val banner = Query.select<Attachment>()
            .join(AttachmentTable.SQL_JOIN_TOOL)
            .where(
                ToolTable.FIELD_CODE.eq(tool)
                    .and(ToolTable.FIELD_BANNER.eq(AttachmentTable.FIELD_ID))
                    .and(AttachmentTable.SQL_WHERE_DOWNLOADED)
            )
            .limit(1)
            .getAsLiveData(dao)
            .map { it.firstOrNull() }

        private val primaryTranslation =
            settings.primaryLanguageLiveData.switchMap { dao.getLatestTranslationLiveData(tool, it) }
        private val defaultTranslation = dao.getLatestTranslationLiveData(tool, Settings.defaultLanguage)
        internal val firstTranslation = primaryTranslation.combineWith(defaultTranslation) { p, d -> p ?: d }
        internal val parallelTranslation =
            settings.parallelLanguageLiveData.switchMap { dao.getLatestTranslationLiveData(tool, it) }

        internal val parallelLanguage = parallelTranslation.switchMap { t ->
            t?.languageCode?.let { dao.findLiveData<Language>(it) }.orEmpty()
        }

        internal val downloadProgress =
            primaryTranslation.switchCombineWith(defaultTranslation, parallelTranslation) { prim, def, para ->
                when {
                    prim != null -> downloadManager.getDownloadProgressLiveData(tool, prim.languageCode)
                    def != null -> downloadManager.getDownloadProgressLiveData(tool, def.languageCode)
                    para != null -> downloadManager.getDownloadProgressLiveData(tool, para.languageCode)
                    else -> emptyLiveData()
                }
            }
    }
}
