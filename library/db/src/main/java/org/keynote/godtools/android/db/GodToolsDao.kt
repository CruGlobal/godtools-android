package org.keynote.godtools.android.db

import android.content.Context
import org.ccci.gto.android.common.db.LiveDataDao
import org.ccci.gto.android.common.db.LiveDataRegistry
import org.ccci.gto.android.common.db.async.AbstractAsyncDao
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Followup
import org.cru.godtools.model.GlobalActivityAnalytics
import org.cru.godtools.model.Language
import org.cru.godtools.model.LocalFile
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationFile
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.FollowupTable
import org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.Contract.LocalFileTable
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.Contract.TranslationFileTable
import org.keynote.godtools.android.db.Contract.TranslationTable

abstract class GodToolsDaoKotlin(context: Context) : AbstractAsyncDao(GodToolsDatabase.getInstance(context)),
    LiveDataDao {
    override val liveDataRegistry = LiveDataRegistry()

    init {
        registerType(
            Followup::class.java, FollowupTable.TABLE_NAME, FollowupTable.PROJECTION_ALL, FollowupMapper(),
            FollowupTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            Language::class.java, LanguageTable.TABLE_NAME, LanguageTable.PROJECTION_ALL, LanguageMapper,
            LanguageTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            Tool::class.java, ToolTable.TABLE_NAME, ToolTable.PROJECTION_ALL, ToolMapper(),
            ToolTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            Attachment::class.java, AttachmentTable.TABLE_NAME, AttachmentTable.PROJECTION_ALL, AttachmentMapper(),
            AttachmentTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            Translation::class.java, TranslationTable.TABLE_NAME, TranslationTable.PROJECTION_ALL, TranslationMapper(),
            TranslationTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            LocalFile::class.java, LocalFileTable.TABLE_NAME, LocalFileTable.PROJECTION_ALL, LocalFileMapper,
            LocalFileTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            TranslationFile::class.java, TranslationFileTable.TABLE_NAME, TranslationFileTable.PROJECTION_ALL,
            TranslationFileMapper(), TranslationFileTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            GlobalActivityAnalytics::class.java, GlobalActivityAnalyticsTable.TABLE_NAME,
            GlobalActivityAnalyticsTable.PROJECTION_ALL, GlobalActivityAnalyticsMapper,
            GlobalActivityAnalyticsTable.SQL_WHERE_PRIMARY_KEY
        )
    }
}
