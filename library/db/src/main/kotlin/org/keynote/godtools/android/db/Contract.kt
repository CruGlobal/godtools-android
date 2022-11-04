package org.keynote.godtools.android.db

import org.ccci.gto.android.common.db.BaseContract
import org.ccci.gto.android.common.db.BaseContract.Base.Companion.COLUMN_ROWID
import org.ccci.gto.android.common.db.BaseContract.Base.Companion.SQL_COLUMN_ROWID
import org.ccci.gto.android.common.db.Expression
import org.ccci.gto.android.common.db.Expression.Companion.bind
import org.ccci.gto.android.common.db.Table
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Followup
import org.cru.godtools.model.Language
import org.cru.godtools.model.LocalFile
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationFile
import org.keynote.godtools.android.db.Contract.BaseTable.LanguageCode
import org.keynote.godtools.android.db.Contract.BaseTable.ToolCode

object Contract : BaseContract() {
    abstract class BaseTable : Base {
        internal companion object {
            internal const val COLUMN_ID = COLUMN_ROWID
            internal const val SQL_COLUMN_ID = SQL_COLUMN_ROWID
        }

        internal object ToolCode {
            internal const val COLUMN_TOOL = "tool"
            internal const val SQL_COLUMN_TOOL = "$COLUMN_TOOL TEXT NOT NULL"
        }

        internal object LanguageCode {
            internal const val COLUMN_LANGUAGE = "language"
            internal const val SQL_COLUMN_LANGUAGE = "$COLUMN_LANGUAGE TEXT NOT NULL"
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

    object LanguageTable : BaseTable() {
        internal const val TABLE_NAME = "languages"
        internal val TABLE = Table.forClass<Language>()

        const val COLUMN_ID = BaseTable.COLUMN_ID
        const val COLUMN_CODE = "code"
        const val COLUMN_NAME = "name"

        val FIELD_ID = TABLE.field(COLUMN_ID)
        val FIELD_CODE = TABLE.field(COLUMN_CODE)

        internal val PROJECTION_ALL = arrayOf(COLUMN_ID, COLUMN_CODE, COLUMN_NAME)

        private const val SQL_COLUMN_CODE = "$COLUMN_CODE TEXT NOT NULL"
        private const val SQL_COLUMN_NAME = "$COLUMN_NAME TEXT"
        private val SQL_PRIMARY_KEY = uniqueIndex(COLUMN_CODE)

        val SQL_JOIN_TRANSLATION = TABLE.join(TranslationTable.TABLE).on(FIELD_CODE.eq(TranslationTable.FIELD_LANGUAGE))

        internal val SQL_WHERE_PRIMARY_KEY = FIELD_CODE.eq(bind())

        internal val SQL_CREATE_TABLE =
            create(TABLE_NAME, SQL_COLUMN_ROWID, SQL_COLUMN_CODE, SQL_COLUMN_NAME, SQL_PRIMARY_KEY)
        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    object ToolTable : BaseTable() {
        internal const val TABLE_NAME = "tools"
        internal val TABLE = Table.forClass<Tool>()
        val TABLE_META = TABLE.`as`("meta")

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

        internal val FIELD_ID = TABLE.field(COLUMN_ID)
        val FIELD_CODE = TABLE.field(COLUMN_CODE)
        val FIELD_TYPE = TABLE.field(COLUMN_TYPE)
        val FIELD_BANNER = TABLE.field(COLUMN_BANNER)
        val FIELD_DETAILS_BANNER = TABLE.field(COLUMN_DETAILS_BANNER)
        val FIELD_DETAILS_BANNER_ANIMATION = TABLE.field(COLUMN_DETAILS_BANNER_ANIMATION)
        val FIELD_META_TOOL = TABLE.field(COLUMN_META_TOOL)
        val FIELD_ADDED = TABLE.field(COLUMN_ADDED)
        val FIELD_HIDDEN = TABLE.field(COLUMN_HIDDEN)
        val FIELD_SPOTLIGHT = TABLE.field(COLUMN_SPOTLIGHT)
        private val FIELD_PENDING_SHARES = TABLE.field(COLUMN_PENDING_SHARES)

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

        private const val SQL_COLUMN_CODE = "$COLUMN_CODE TEXT"
        private const val SQL_COLUMN_TYPE = "$COLUMN_TYPE TEXT"
        private const val SQL_COLUMN_NAME = "$COLUMN_NAME TEXT"
        private const val SQL_COLUMN_DESCRIPTION = "$COLUMN_DESCRIPTION TEXT"
        private const val SQL_COLUMN_CATEGORY = "$COLUMN_CATEGORY TEXT"
        private const val SQL_COLUMN_SHARES = "$COLUMN_SHARES INTEGER"
        private const val SQL_COLUMN_PENDING_SHARES = "$COLUMN_PENDING_SHARES INTEGER"
        private const val SQL_COLUMN_BANNER = "$COLUMN_BANNER INTEGER"
        private const val SQL_COLUMN_DETAILS_BANNER = "$COLUMN_DETAILS_BANNER INTEGER"
        private const val SQL_COLUMN_DETAILS_BANNER_ANIMATION = "$COLUMN_DETAILS_BANNER_ANIMATION INTEGER"
        private const val SQL_COLUMN_DETAILS_BANNER_YOUTUBE = "$COLUMN_DETAILS_BANNER_YOUTUBE TEXT"
        private const val SQL_COLUMN_SCREEN_SHARE_DISABLED = "$COLUMN_SCREEN_SHARE_DISABLED INTEGER"
        private const val SQL_COLUMN_DEFAULT_ORDER = "$COLUMN_DEFAULT_ORDER INTEGER"
        private const val SQL_COLUMN_ORDER = "$COLUMN_ORDER INTEGER"
        private const val SQL_COLUMN_META_TOOL = "$COLUMN_META_TOOL TEXT"
        private const val SQL_COLUMN_DEFAULT_VARIANT = "$COLUMN_DEFAULT_VARIANT TEXT"
        private const val SQL_COLUMN_ADDED = "$COLUMN_ADDED INTEGER"
        private const val SQL_COLUMN_HIDDEN = "$COLUMN_HIDDEN INTEGER"
        private const val SQL_COLUMN_SPOTLIGHT = "$COLUMN_SPOTLIGHT INTEGER"
        private val SQL_PRIMARY_KEY = uniqueIndex(COLUMN_CODE)

        val SQL_JOIN_METATOOL =
            TABLE.join(TABLE_META).on(TABLE_META.field(COLUMN_CODE).eq(TABLE.field(COLUMN_META_TOOL)))

        internal val SQL_WHERE_PRIMARY_KEY = FIELD_CODE.eq(bind())
        val SQL_WHERE_HAS_PENDING_SHARES = FIELD_PENDING_SHARES.gt(0)
        const val SQL_ORDER_BY_ORDER = "$COLUMN_ORDER,$COLUMN_DEFAULT_ORDER"

        internal val SQL_CREATE_TABLE = create(
            TABLE_NAME,
            SQL_COLUMN_ID,
            SQL_COLUMN_CODE,
            SQL_COLUMN_TYPE,
            SQL_COLUMN_NAME,
            SQL_COLUMN_DESCRIPTION,
            SQL_COLUMN_CATEGORY,
            SQL_COLUMN_SHARES,
            SQL_COLUMN_PENDING_SHARES,
            SQL_COLUMN_BANNER,
            SQL_COLUMN_DETAILS_BANNER,
            SQL_COLUMN_DETAILS_BANNER_ANIMATION,
            SQL_COLUMN_DETAILS_BANNER_YOUTUBE,
            SQL_COLUMN_SCREEN_SHARE_DISABLED,
            SQL_COLUMN_DEFAULT_ORDER,
            SQL_COLUMN_ORDER,
            SQL_COLUMN_META_TOOL,
            SQL_COLUMN_DEFAULT_VARIANT,
            SQL_COLUMN_ADDED,
            SQL_COLUMN_HIDDEN,
            SQL_COLUMN_SPOTLIGHT,
            SQL_PRIMARY_KEY
        )
        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)

        // region DB migrations
        internal const val SQL_V45_ALTER_HIDDEN = "ALTER TABLE $TABLE_NAME ADD COLUMN $SQL_COLUMN_HIDDEN"
        internal const val SQL_V45_POPULATE_HIDDEN = "UPDATE $TABLE_NAME SET $COLUMN_HIDDEN = 0"
        internal const val SQL_V47_ALTER_SCREEN_SHARE_DISABLED =
            "ALTER TABLE $TABLE_NAME ADD COLUMN $SQL_COLUMN_SCREEN_SHARE_DISABLED"
        internal const val SQL_V48_CREATE_SPOTLIGHT = "ALTER TABLE $TABLE_NAME ADD COLUMN $SQL_COLUMN_SPOTLIGHT"
        internal const val SQL_V48_POPULATE_SPOTLIGHT = "UPDATE $TABLE_NAME SET $COLUMN_SPOTLIGHT = 0"
        internal const val SQL_V49_ALTER_DETAILS_BANNER_ANIMATION =
            "ALTER TABLE $TABLE_NAME ADD COLUMN $SQL_COLUMN_DETAILS_BANNER_ANIMATION"
        internal const val SQL_V50_ALTER_META_TOOL = "ALTER TABLE $TABLE_NAME ADD COLUMN $SQL_COLUMN_META_TOOL"
        internal const val SQL_V51_ALTER_DEFAULT_VARIANT =
            "ALTER TABLE $TABLE_NAME ADD COLUMN $SQL_COLUMN_DEFAULT_VARIANT"
        // endregion DB migrations
    }

    object TranslationTable : BaseTable() {
        internal const val TABLE_NAME = "translations"
        internal val TABLE = Table.forClass<Translation>()

        const val COLUMN_TOOL = ToolCode.COLUMN_TOOL
        const val COLUMN_LANGUAGE = LanguageCode.COLUMN_LANGUAGE
        const val COLUMN_VERSION = "version"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_TAGLINE = "tagline"
        const val COLUMN_MANIFEST = "manifest"
        const val COLUMN_PUBLISHED = "published"
        const val COLUMN_DOWNLOADED = "downloaded"
        internal const val COLUMN_LAST_ACCESSED = "last_accessed"

        val FIELD_ID = TABLE.field(COLUMN_ID)
        val FIELD_TOOL = TABLE.field(COLUMN_TOOL)
        val FIELD_LANGUAGE = TABLE.field(COLUMN_LANGUAGE)
        val FIELD_MANIFEST = TABLE.field(COLUMN_MANIFEST)
        private val FIELD_PUBLISHED = TABLE.field(COLUMN_PUBLISHED)
        val FIELD_DOWNLOADED = TABLE.field(COLUMN_DOWNLOADED)

        internal val PROJECTION_ALL = arrayOf(
            COLUMN_ID,
            COLUMN_TOOL,
            COLUMN_LANGUAGE,
            COLUMN_VERSION,
            COLUMN_NAME,
            COLUMN_DESCRIPTION,
            COLUMN_TAGLINE,
            COLUMN_MANIFEST,
            COLUMN_PUBLISHED,
            COLUMN_DOWNLOADED,
            COLUMN_LAST_ACCESSED
        )

        private const val SQL_COLUMN_VERSION = "$COLUMN_VERSION INTEGER"
        private const val SQL_COLUMN_NAME = "$COLUMN_NAME TEXT"
        private const val SQL_COLUMN_DESCRIPTION = "$COLUMN_DESCRIPTION TEXT"
        private const val SQL_COLUMN_TAGLINE = "$COLUMN_TAGLINE TEXT"
        private const val SQL_COLUMN_MANIFEST = "$COLUMN_MANIFEST TEXT"
        private const val SQL_COLUMN_PUBLISHED = "$COLUMN_PUBLISHED INTEGER"
        private const val SQL_COLUMN_DOWNLOADED = "$COLUMN_DOWNLOADED INTEGER"
        private const val SQL_COLUMN_LAST_ACCESSED = "$COLUMN_LAST_ACCESSED INTEGER"

        val SQL_JOIN_LANGUAGE = TABLE.join(LanguageTable.TABLE).on(FIELD_LANGUAGE.eq(LanguageTable.FIELD_CODE))
        val SQL_JOIN_TOOL = TABLE.join(ToolTable.TABLE).on(FIELD_TOOL.eq(ToolTable.FIELD_CODE))

        internal val SQL_WHERE_PRIMARY_KEY = FIELD_ID.eq(bind())
        internal val SQL_WHERE_TOOL_LANGUAGE = FIELD_TOOL.eq(bind()).and(FIELD_LANGUAGE.eq(bind()))
        val SQL_WHERE_PUBLISHED = FIELD_PUBLISHED.eq(true)
        val SQL_WHERE_DOWNLOADED = FIELD_DOWNLOADED.eq(true)
        const val SQL_ORDER_BY_VERSION_DESC = "$COLUMN_VERSION DESC"

        internal val SQL_CREATE_TABLE = create(
            TABLE_NAME,
            SQL_COLUMN_ID,
            ToolCode.SQL_COLUMN_TOOL,
            LanguageCode.SQL_COLUMN_LANGUAGE,
            SQL_COLUMN_VERSION,
            SQL_COLUMN_NAME,
            SQL_COLUMN_DESCRIPTION,
            SQL_COLUMN_TAGLINE,
            SQL_COLUMN_MANIFEST,
            SQL_COLUMN_PUBLISHED,
            SQL_COLUMN_DOWNLOADED,
            SQL_COLUMN_LAST_ACCESSED
        )
        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    object AttachmentTable : BaseTable() {
        internal const val TABLE_NAME = "attachments"
        internal val TABLE = Table.forClass<Attachment>()

        const val COLUMN_TOOL = "tool"
        const val COLUMN_FILENAME = "filename"
        const val COLUMN_SHA256 = "sha256"
        internal const val COLUMN_LOCALFILENAME = "local_filename"
        const val COLUMN_DOWNLOADED = "downloaded"

        val FIELD_ID = TABLE.field(COLUMN_ID)
        val FIELD_TOOL = TABLE.field(COLUMN_TOOL)
        internal val FIELD_LOCALFILENAME = TABLE.field(COLUMN_LOCALFILENAME)
        val FIELD_DOWNLOADED = TABLE.field(COLUMN_DOWNLOADED)

        internal val PROJECTION_ALL =
            arrayOf(COLUMN_ID, COLUMN_TOOL, COLUMN_FILENAME, COLUMN_SHA256, COLUMN_LOCALFILENAME, COLUMN_DOWNLOADED)

        private const val SQL_COLUMN_TOOL = "$COLUMN_TOOL INTEGER"
        private const val SQL_COLUMN_FILENAME = "$COLUMN_FILENAME TEXT"
        private const val SQL_COLUMN_SHA256 = "$COLUMN_SHA256 TEXT"
        private const val SQL_COLUMN_LOCALFILENAME = "$COLUMN_LOCALFILENAME TEXT"
        private const val SQL_COLUMN_DOWNLOADED = "$COLUMN_DOWNLOADED INTEGER"

        val SQL_JOIN_TOOL = TABLE.join(ToolTable.TABLE).on(FIELD_TOOL.eq(ToolTable.FIELD_ID))
        val SQL_JOIN_LOCAL_FILE = TABLE.join(LocalFileTable.TABLE).on(FIELD_LOCALFILENAME.eq(LocalFileTable.FIELD_NAME))

        internal val SQL_WHERE_PRIMARY_KEY = FIELD_ID.eq(bind())
        val SQL_WHERE_DOWNLOADED = FIELD_DOWNLOADED.eq(true)
        val SQL_WHERE_NOT_DOWNLOADED = FIELD_DOWNLOADED.eq(false)

        internal val SQL_CREATE_TABLE = create(
            TABLE_NAME,
            SQL_COLUMN_ID,
            SQL_COLUMN_TOOL,
            SQL_COLUMN_FILENAME,
            SQL_COLUMN_SHA256,
            SQL_COLUMN_LOCALFILENAME,
            SQL_COLUMN_DOWNLOADED
        )
        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    object LocalFileTable : Base {
        internal const val TABLE_NAME = "files"
        internal val TABLE = Table.forClass<LocalFile>()

        internal const val COLUMN_NAME = "name"

        val FIELD_NAME = TABLE.field(COLUMN_NAME)

        internal val PROJECTION_ALL = arrayOf(COLUMN_NAME)

        private const val SQL_COLUMN_NAME = "$COLUMN_NAME TEXT"
        private val SQL_PRIMARY_KEY = uniqueIndex(COLUMN_NAME)

        val SQL_JOIN_ATTACHMENT =
            TABLE.join(AttachmentTable.TABLE).on(FIELD_NAME.eq(AttachmentTable.FIELD_LOCALFILENAME))
        val SQL_JOIN_TRANSLATION_FILE =
            TABLE.join(TranslationFileTable.TABLE).on(FIELD_NAME.eq(TranslationFileTable.FIELD_FILE))

        internal val SQL_WHERE_PRIMARY_KEY: Expression = FIELD_NAME.eq(bind())

        internal val SQL_CREATE_TABLE = create(TABLE_NAME, SQL_COLUMN_NAME, SQL_PRIMARY_KEY)
        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    object TranslationFileTable : Base {
        internal const val TABLE_NAME = "translation_files"
        internal val TABLE = Table.forClass<TranslationFile>()

        internal const val COLUMN_TRANSLATION = "translation"
        internal const val COLUMN_FILE = "file"

        private val FIELD_TRANSLATION = TABLE.field(COLUMN_TRANSLATION)
        val FIELD_FILE = TABLE.field(COLUMN_FILE)

        internal val PROJECTION_ALL = arrayOf(COLUMN_TRANSLATION, COLUMN_FILE)

        private const val SQL_COLUMN_TRANSLATION = "$COLUMN_TRANSLATION INTEGER NOT NULL"
        private const val SQL_COLUMN_NAME = "$COLUMN_FILE TEXT NOT NULL"
        private val SQL_PRIMARY_KEY = uniqueIndex(COLUMN_TRANSLATION, COLUMN_FILE)

        val SQL_JOIN_TRANSLATION =
            TABLE.join(TranslationTable.TABLE).on(FIELD_TRANSLATION.eq(TranslationTable.FIELD_ID))

        internal val SQL_WHERE_PRIMARY_KEY = FIELD_TRANSLATION.eq(bind()).and(FIELD_FILE.eq(bind()))

        internal val SQL_CREATE_TABLE = create(TABLE_NAME, SQL_COLUMN_TRANSLATION, SQL_COLUMN_NAME, SQL_PRIMARY_KEY)
        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    internal object FollowupTable : BaseTable() {
        const val TABLE_NAME = "followups"
        private val TABLE = Table.forClass<Followup>()

        const val COLUMN_NAME = "name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_DESTINATION = "destination"
        const val COLUMN_LANGUAGE = "language"
        const val COLUMN_CREATE_TIME = "created_at"

        private val FIELD_ID = TABLE.field(COLUMN_ID)

        val PROJECTION_ALL = arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_DESTINATION, COLUMN_LANGUAGE)

        private const val SQL_COLUMN_NAME = "$COLUMN_NAME TEXT"
        private const val SQL_COLUMN_EMAIL = "$COLUMN_EMAIL TEXT"
        private const val SQL_COLUMN_DESTINATION = "$COLUMN_DESTINATION INTEGER"
        private const val SQL_COLUMN_LANGUAGE = "$COLUMN_LANGUAGE TEXT NOT NULL"
        private const val SQL_COLUMN_CREATE_TIME = "$COLUMN_CREATE_TIME INTEGER"

        val SQL_WHERE_PRIMARY_KEY = FIELD_ID.eq(bind())

        val SQL_CREATE_TABLE = create(
            TABLE_NAME,
            SQL_COLUMN_ID,
            SQL_COLUMN_NAME,
            SQL_COLUMN_EMAIL,
            SQL_COLUMN_DESTINATION,
            SQL_COLUMN_LANGUAGE,
            SQL_COLUMN_CREATE_TIME
        )
        val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    internal object GlobalActivityAnalyticsTable : BaseTable() {
        internal const val TABLE_NAME = "global_activity_analytics"

        internal const val COLUMN_USERS = "users"
        internal const val COLUMN_COUNTRIES = "countries"
        internal const val COLUMN_LAUNCHES = "launches"
        internal const val COLUMN_GOSPEL_PRESENTATIONS = "gospel_presentations"

        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    internal object TrainingTipTable : Base {
        internal const val TABLE_NAME = "training_tips"

        internal const val COLUMN_TOOL = ToolCode.COLUMN_TOOL
        internal const val COLUMN_LANGUAGE = LanguageCode.COLUMN_LANGUAGE
        internal const val COLUMN_TIP_ID = "tipId"
        const val COLUMN_IS_COMPLETED = "isCompleted"

        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)
    }

    internal object UserCounterTable : Base {
        internal const val TABLE_NAME = "user_counters"

        internal const val COLUMN_COUNTER_ID = "counter_id"
        const val COLUMN_COUNT = "count"
        const val COLUMN_DECAYED_COUNT = "decayed_count"
        internal const val COLUMN_DELTA = "delta"

        private const val SQL_COLUMN_COUNTER_ID = "$COLUMN_COUNTER_ID TEXT"
        private const val SQL_COLUMN_COUNT = "$COLUMN_COUNT INTEGER"
        private const val SQL_COLUMN_DECAYED_COUNT = "$COLUMN_DECAYED_COUNT REAL"
        private const val SQL_COLUMN_DELTA = "$COLUMN_DELTA INTEGER"
        private val SQL_PRIMARY_KEY = uniqueIndex(COLUMN_COUNTER_ID)

        internal val SQL_DELETE_TABLE = drop(TABLE_NAME)

        // region DB Migrations
        internal val SQL_V46_CREATE_USER_COUNTERS = create(
            TABLE_NAME,
            SQL_COLUMN_ROWID,
            SQL_COLUMN_COUNTER_ID,
            SQL_COLUMN_COUNT,
            SQL_COLUMN_DECAYED_COUNT,
            SQL_COLUMN_DELTA,
            SQL_PRIMARY_KEY
        )
        // endregion DB Migrations
    }
}
