package org.cru.godtools.ui.tools

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
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
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao
import javax.inject.Inject

class ToolsAdapterToolViewModel @Inject constructor(
    private val dao: GodToolsDao,
    private val downloadManager: GodToolsDownloadManager,
    settings: Settings
) : ViewModel() {
    val toolCode = MutableLiveData<String?>()
    private val distinctToolCode = toolCode.distinctUntilChanged()

    @Deprecated("This is temporary until ToolsAdapter stops using a Cursor")
    val tool by lazy { distinctToolCode.switchMap { it?.let { dao.findLiveData<Tool>(it) }.orEmpty() } }

    val banner = distinctToolCode.switchMap {
        it?.let {
            Query.select<Attachment>()
                .join(AttachmentTable.SQL_JOIN_TOOL)
                .where(
                    ToolTable.FIELD_CODE.eq(it)
                        .and(ToolTable.FIELD_BANNER.eq(AttachmentTable.FIELD_ID))
                        .and(AttachmentTable.SQL_WHERE_DOWNLOADED)
                )
                .limit(1)
                .getAsLiveData(dao)
        }.orEmpty()
    }.map { it?.firstOrNull() }

    private val primaryTranslation = distinctToolCode.switchCombineWith(settings.primaryLanguageLiveData) { c, l ->
        dao.getLatestTranslationLiveData(c, l)
    }
    private val defaultTranslation =
        distinctToolCode.switchMap { dao.getLatestTranslationLiveData(it, Settings.defaultLanguage) }
    val firstTranslation = primaryTranslation.combineWith(defaultTranslation) { p, d -> p ?: d }
    val parallelTranslation = distinctToolCode.switchCombineWith(settings.parallelLanguageLiveData) { c, l ->
        dao.getLatestTranslationLiveData(c, l)
    }

    val firstLanguage = firstTranslation.languageLiveData
    val parallelLanguage = parallelTranslation.languageLiveData

    val downloadProgress = distinctToolCode.switchCombineWith(
        primaryTranslation, defaultTranslation, parallelTranslation
    ) { code, primary, default, parallel ->
        when {
            code == null -> emptyLiveData()
            primary != null -> downloadManager.getDownloadProgressLiveData(code, primary.languageCode)
            default != null -> downloadManager.getDownloadProgressLiveData(code, default.languageCode)
            parallel != null -> downloadManager.getDownloadProgressLiveData(code, parallel.languageCode)
            else -> emptyLiveData()
        }
    }

    private val LiveData<Translation?>.languageLiveData
        get() = switchMap { t ->
            t?.languageCode
                ?.let { Query.select<Language>().where(LanguageTable.FIELD_CODE.eq(it)).getAsLiveData(dao) }
                .orEmpty()
        }.map { it?.firstOrNull() }
}
