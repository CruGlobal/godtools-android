package org.keynote.godtools.android.db

import org.ccci.gto.android.common.db.BaseContract
import org.ccci.gto.android.common.db.BaseContract.Base.Companion.COLUMN_ROWID
import org.keynote.godtools.android.db.Contract.BaseTable.LanguageCode
import org.keynote.godtools.android.db.Contract.BaseTable.ToolCode

internal object Contract : BaseContract() {
    abstract class BaseTable : Base {
        internal companion object {
            internal const val COLUMN_ID = COLUMN_ROWID
        }

        internal object ToolCode {
            internal const val COLUMN_TOOL = "tool"
        }

        internal object LanguageCode {
            internal const val COLUMN_LANGUAGE = "language"
        }
    }

    internal object LegacyTables {
        val SQL_DELETE_GSSUBSCRIBERS = drop("gssubscribers")
        val SQL_DELETE_GTLANGUAGES = drop("gtlanguages")
        val SQL_DELETE_GTLANGUAGES_OLD = drop("gtlanguages_old")
        val SQL_DELETE_GTPACKAGES = drop("gtpackages")
        val SQL_DELETE_GTPACKAGES_OLD = drop("gtpackages_old")
        val SQL_DELETE_RESOURCES = drop("resources")
    }

    internal object LanguageTable : BaseTable() {
        internal const val TABLE_NAME = "languages"

        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    internal object ToolTable : BaseTable() {
        internal const val TABLE_NAME = "tools"

        const val COLUMN_CODE = "code"
        const val COLUMN_TYPE = "type"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_SHARES = "shares"
        internal const val COLUMN_PENDING_SHARES = "pending_shares"
        const val COLUMN_BANNER = "banner"
        const val COLUMN_DETAILS_BANNER = "banner_details"
        const val COLUMN_DETAILS_BANNER_ANIMATION = "details_banner_animation"
        const val COLUMN_DETAILS_BANNER_YOUTUBE = "overview_video"
        const val COLUMN_SCREEN_SHARE_DISABLED = "screen_share_disabled"
        const val COLUMN_DEFAULT_ORDER = "default_order"
        internal const val COLUMN_ORDER = "ordering"
        const val COLUMN_META_TOOL = "metatool"
        const val COLUMN_DEFAULT_VARIANT = "default_variant"
        const val COLUMN_ADDED = "added"
        const val COLUMN_HIDDEN = "isHidden"
        const val COLUMN_SPOTLIGHT = "isSpotlight"

        internal val PROJECTION_ALL = arrayOf(
            COLUMN_ID,
            COLUMN_CODE,
            COLUMN_TYPE,
            COLUMN_NAME,
            COLUMN_DESCRIPTION,
            COLUMN_CATEGORY,
            COLUMN_SHARES,
            COLUMN_PENDING_SHARES,
            COLUMN_BANNER,
            COLUMN_DETAILS_BANNER,
            COLUMN_DETAILS_BANNER_ANIMATION,
            COLUMN_DETAILS_BANNER_YOUTUBE,
            COLUMN_SCREEN_SHARE_DISABLED,
            COLUMN_DEFAULT_ORDER,
            COLUMN_ORDER,
            COLUMN_META_TOOL,
            COLUMN_DEFAULT_VARIANT,
            COLUMN_ADDED,
            COLUMN_HIDDEN,
            COLUMN_SPOTLIGHT
        )

        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    internal object TranslationTable : BaseTable() {
        internal const val TABLE_NAME = "translations"

        const val COLUMN_TOOL = ToolCode.COLUMN_TOOL
        const val COLUMN_LANGUAGE = LanguageCode.COLUMN_LANGUAGE
        const val COLUMN_VERSION = "version"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_TAGLINE = "tagline"
        const val COLUMN_DETAILS_OUTLINE = "detailsOutline"
        const val COLUMN_DETAILS_BIBLE_REFERENCES = "detailsBibleReferences"
        const val COLUMN_DETAILS_CONVERSATION_STARTERS = "detailsConversationStarters"
        const val COLUMN_MANIFEST = "manifest"
        const val COLUMN_PUBLISHED = "published"
        const val COLUMN_DOWNLOADED = "downloaded"

        internal val PROJECTION_ALL = arrayOf(
            COLUMN_ID,
            COLUMN_TOOL,
            COLUMN_LANGUAGE,
            COLUMN_VERSION,
            COLUMN_NAME,
            COLUMN_DESCRIPTION,
            COLUMN_TAGLINE,
            COLUMN_DETAILS_OUTLINE,
            COLUMN_DETAILS_BIBLE_REFERENCES,
            COLUMN_DETAILS_CONVERSATION_STARTERS,
            COLUMN_MANIFEST,
            COLUMN_PUBLISHED,
            COLUMN_DOWNLOADED,
        )

        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    internal object AttachmentTable : BaseTable() {
        internal const val TABLE_NAME = "attachments"

        const val COLUMN_TOOL = "tool"
        const val COLUMN_FILENAME = "filename"
        const val COLUMN_SHA256 = "sha256"
        internal const val COLUMN_LOCALFILENAME = "local_filename"
        const val COLUMN_DOWNLOADED = "downloaded"

        internal val PROJECTION_ALL = arrayOf(COLUMN_ID, COLUMN_TOOL, COLUMN_FILENAME, COLUMN_SHA256, COLUMN_DOWNLOADED)

        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    internal object DownloadedFileTable : Base {
        internal const val TABLE_NAME = "files"
        internal const val COLUMN_NAME = "name"

        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    internal object TranslationFileTable : Base {
        internal const val TABLE_NAME = "translation_files"
        internal const val COLUMN_TRANSLATION = "translation"
        internal const val COLUMN_FILE = "file"

        internal val PROJECTION_ALL = arrayOf(COLUMN_TRANSLATION, COLUMN_FILE)

        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    internal object FollowupTable : BaseTable() {
        const val TABLE_NAME = "followups"

        val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    internal object GlobalActivityAnalyticsTable : BaseTable() {
        internal const val TABLE_NAME = "global_activity_analytics"

        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    internal object TrainingTipTable : Base {
        internal const val TABLE_NAME = "training_tips"

        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    internal object UserCounterTable : Base {
        internal const val TABLE_NAME = "user_counters"

        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }
}
