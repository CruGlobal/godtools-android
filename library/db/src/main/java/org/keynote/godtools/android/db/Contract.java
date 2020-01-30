package org.keynote.godtools.android.db;

import org.ccci.gto.android.common.db.BaseContract;
import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Expression.Field;
import org.ccci.gto.android.common.db.Join;
import org.ccci.gto.android.common.db.Table;
import org.cru.godtools.model.Attachment;
import org.cru.godtools.model.Followup;
import org.cru.godtools.model.GlobalActivityAnalytics;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.LocalFile;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.TranslationFile;

import static org.ccci.gto.android.common.db.Expression.bind;

public final class Contract extends BaseContract {
    public abstract static class BaseTable implements Base {
        public static final String COLUMN_ID = COLUMN_ROWID;
        static final String SQL_COLUMN_ID = SQL_COLUMN_ROWID;
    }

    static class LegacyTables {
        static final String SQL_DELETE_GSSUBSCRIBERS = drop("gssubscribers");
        static final String SQL_DELETE_GTLANGUAGES = drop("gtlanguages");
        static final String SQL_DELETE_GTLANGUAGES_OLD = drop("gtlanguages_old");
        static final String SQL_DELETE_GTPACKAGES = drop("gtpackages");
        static final String SQL_DELETE_GTPACKAGES_OLD = drop("gtpackages_old");
        static final String SQL_DELETE_RESOURCES = drop("resources");
    }

    @SuppressWarnings("checkstyle:InterfaceIsType")
    interface ToolId {
        String COLUMN_TOOL = "tool";
        String SQL_COLUMN_TOOL = COLUMN_TOOL + " INTEGER";
    }

    @SuppressWarnings("checkstyle:InterfaceIsType")
    interface ToolCode {
        String COLUMN_TOOL = "tool";
        String SQL_COLUMN_TOOL = COLUMN_TOOL + " TEXT";
    }

    public static class LanguageTable extends BaseTable {
        static final String TABLE_NAME = "languages";
        static final Table<Language> TABLE = Table.forClass(Language.class);

        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_ADDED = "added";
        public static final String COLUMN_NAME = "name";

        public static final Field FIELD_ID = TABLE.field(COLUMN_ID);
        public static final Field FIELD_CODE = TABLE.field(COLUMN_CODE);
        public static final Field FIELD_ADDED = TABLE.field(COLUMN_ADDED);
        public static final Field FIELD_NAME = TABLE.field(COLUMN_NAME);

        static final String[] PROJECTION_ALL = {COLUMN_ID, COLUMN_CODE, COLUMN_ADDED, COLUMN_NAME};

        private static final String SQL_COLUMN_CODE = COLUMN_CODE + " TEXT NOT NULL";
        private static final String SQL_COLUMN_ADDED = COLUMN_ADDED + " INTEGER";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_PRIMARY_KEY = uniqueIndex(COLUMN_CODE);

        public static final Join<Language, Translation> SQL_JOIN_TRANSLATION =
                TABLE.join(TranslationTable.TABLE).on(FIELD_CODE.eq(TranslationTable.FIELD_LANGUAGE));

        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_CODE.eq(bind());
        public static final Expression SQL_WHERE_ADDED = FIELD_ADDED.eq(true);

        static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ROWID, SQL_COLUMN_CODE, SQL_COLUMN_ADDED,
                        SQL_COLUMN_NAME, SQL_PRIMARY_KEY);
        static final String SQL_DELETE_TABLE = drop(TABLE_NAME);

        static final String SQL_V39_ALTER_NAME = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_NAME;
    }

    public static class ToolTable extends BaseTable {
        public static final String TABLE_NAME = "tools";
        static final Table<Tool> TABLE = Table.forClass(Tool.class);

        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_SHARES = "shares";
        public static final String COLUMN_PENDING_SHARES = "pending_shares";
        public static final String COLUMN_BANNER = "banner";
        public static final String COLUMN_DETAILS_BANNER = "banner_details";
        public static final String COLUMN_OVERVIEW_VIDEO = "overview_video";
        public static final String COLUMN_COPYRIGHT = "copyright";
        public static final String COLUMN_ORDER = "ordering";
        public static final String COLUMN_ADDED = "added";

        static final Field FIELD_ID = TABLE.field(COLUMN_ID);
        public static final Field FIELD_CODE = TABLE.field(COLUMN_CODE);
        public static final Field FIELD_TYPE = TABLE.field(COLUMN_TYPE);
        public static final Field FIELD_BANNER = TABLE.field(COLUMN_BANNER);
        public static final Field FIELD_DETAILS_BANNER = TABLE.field(COLUMN_DETAILS_BANNER);
        public static final Field FIELD_ADDED = TABLE.field(COLUMN_ADDED);
        private static final Field FIELD_PENDING_SHARES = TABLE.field(COLUMN_PENDING_SHARES);

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_CODE, COLUMN_TYPE, COLUMN_NAME, COLUMN_DESCRIPTION,
                        COLUMN_SHARES, COLUMN_PENDING_SHARES, COLUMN_BANNER, COLUMN_OVERVIEW_VIDEO,
                        COLUMN_DETAILS_BANNER, COLUMN_COPYRIGHT, COLUMN_ORDER, COLUMN_ADDED};

        private static final String SQL_COLUMN_CODE = COLUMN_CODE + " TEXT";
        private static final String SQL_COLUMN_TYPE = COLUMN_TYPE + " TEXT";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_DESCRIPTION = COLUMN_DESCRIPTION + " TEXT";
        private static final String SQL_COLUMN_SHARES = COLUMN_SHARES + " INTEGER";
        private static final String SQL_COLUMN_PENDING_SHARES = COLUMN_PENDING_SHARES + " INTEGER";
        private static final String SQL_COLUMN_BANNER = COLUMN_BANNER + " INTEGER";
        private static final String SQL_COLUMN_DETAILS_BANNER = COLUMN_DETAILS_BANNER + " INTEGER";
        private static final String SQL_COLUMN_OVERVIEW_VIDEO = COLUMN_OVERVIEW_VIDEO + " TEXT";
        private static final String SQL_COLUMN_COPYRIGHT = COLUMN_COPYRIGHT + " TEXT";
        private static final String SQL_COLUMN_ORDER = COLUMN_ORDER + " INTEGER";
        private static final String SQL_COLUMN_ADDED = COLUMN_ADDED + " INTEGER";
        private static final String SQL_PRIMARY_KEY = uniqueIndex(COLUMN_CODE);

        public static final Join<Tool, Attachment> SQL_JOIN_BANNER =
                TABLE.join(AttachmentTable.TABLE).on(FIELD_BANNER.eq(AttachmentTable.FIELD_ID));

        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_CODE.eq(bind());
        public static final Expression SQL_WHERE_HAS_PENDING_SHARES = FIELD_PENDING_SHARES.gt(0);

        static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ID, SQL_COLUMN_CODE, SQL_COLUMN_TYPE, SQL_COLUMN_NAME,
                       SQL_COLUMN_DESCRIPTION, SQL_COLUMN_SHARES, SQL_COLUMN_PENDING_SHARES, SQL_COLUMN_BANNER,
                       SQL_COLUMN_DETAILS_BANNER, SQL_COLUMN_OVERVIEW_VIDEO, SQL_COLUMN_COPYRIGHT, SQL_COLUMN_ORDER,
                       SQL_COLUMN_ADDED, SQL_PRIMARY_KEY);
        static final String SQL_DELETE_TABLE = drop(TABLE_NAME);

        /* DB migrations */
        static final String SQL_V38_ALTER_ORDER = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_ORDER;
        static final String SQL_V38_POPULATE_ORDER =
                "UPDATE " + TABLE_NAME + " SET " + COLUMN_ORDER + " = " + Integer.MAX_VALUE;
        static final String SQL_V40_ALTER_OVERVIEW_VIDEO =
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_OVERVIEW_VIDEO;
    }

    public static class TranslationTable extends BaseTable implements ToolCode {
        static final String TABLE_NAME = "translations";
        public static final Table<Translation> TABLE = Table.forClass(Translation.class);

        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_VERSION = "version";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_TAGLINE = "tagline";
        public static final String COLUMN_MANIFEST = "manifest";
        public static final String COLUMN_PUBLISHED = "published";
        public static final String COLUMN_DOWNLOADED = "downloaded";
        public static final String COLUMN_LAST_ACCESSED = "last_accessed";

        public static final Field FIELD_ID = TABLE.field(COLUMN_ID);
        public static final Field FIELD_TOOL = TABLE.field(COLUMN_TOOL);
        public static final Field FIELD_LANGUAGE = TABLE.field(COLUMN_LANGUAGE);
        public static final Field FIELD_MANIFEST = TABLE.field(COLUMN_MANIFEST);
        private static final Field FIELD_PUBLISHED = TABLE.field(COLUMN_PUBLISHED);
        public static final Field FIELD_DOWNLOADED = TABLE.field(COLUMN_DOWNLOADED);
        public static final Field FIELD_LAST_ACCESSED = TABLE.field(COLUMN_LAST_ACCESSED);

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_TOOL, COLUMN_LANGUAGE, COLUMN_VERSION, COLUMN_NAME, COLUMN_DESCRIPTION,
                        COLUMN_TAGLINE, COLUMN_MANIFEST, COLUMN_PUBLISHED, COLUMN_DOWNLOADED, COLUMN_LAST_ACCESSED};

        private static final String SQL_COLUMN_LANGUAGE = COLUMN_LANGUAGE + " TEXT NOT NULL";
        private static final String SQL_COLUMN_VERSION = COLUMN_VERSION + " INTEGER";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_DESCRIPTION = COLUMN_DESCRIPTION + " TEXT";
        private static final String SQL_COLUMN_TAGLINE = COLUMN_TAGLINE + " TEXT";
        private static final String SQL_COLUMN_MANIFEST = COLUMN_MANIFEST + " TEXT";
        private static final String SQL_COLUMN_PUBLISHED = COLUMN_PUBLISHED + " INTEGER";
        private static final String SQL_COLUMN_DOWNLOADED = COLUMN_DOWNLOADED + " INTEGER";
        private static final String SQL_COLUMN_LAST_ACCESSED = COLUMN_LAST_ACCESSED + " INTEGER";

        public static final Join<Translation, Language> SQL_JOIN_LANGUAGE =
                TABLE.join(LanguageTable.TABLE).on(FIELD_LANGUAGE.eq(LanguageTable.FIELD_CODE));
        public static final Join<Translation, Tool> SQL_JOIN_TOOL =
                TABLE.join(ToolTable.TABLE).on(FIELD_TOOL.eq(ToolTable.FIELD_CODE));

        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_ID.eq(bind());
        public static final Expression SQL_WHERE_TOOL_LANGUAGE = FIELD_TOOL.eq(bind()).and(FIELD_LANGUAGE.eq(bind()));
        public static final Expression SQL_WHERE_PUBLISHED = FIELD_PUBLISHED.eq(true);
        public static final Expression SQL_WHERE_DOWNLOADED = FIELD_DOWNLOADED.eq(true);

        public static final String SQL_ORDER_BY_VERSION_DESC = COLUMN_VERSION + " DESC";

        static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ID, SQL_COLUMN_TOOL, SQL_COLUMN_LANGUAGE, SQL_COLUMN_VERSION,
                       SQL_COLUMN_NAME, SQL_COLUMN_DESCRIPTION, SQL_COLUMN_TAGLINE, SQL_COLUMN_MANIFEST,
                       SQL_COLUMN_PUBLISHED, SQL_COLUMN_DOWNLOADED, SQL_COLUMN_LAST_ACCESSED);
        static final String SQL_DELETE_TABLE = drop(TABLE_NAME);

        /* DB migrations */
        static final String SQL_V37_ALTER_TAGLINE =
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_TAGLINE;
    }

    public static class LocalFileTable implements Base {
        static final String TABLE_NAME = "files";
        static final Table<LocalFile> TABLE = Table.forClass(LocalFile.class);

        static final String COLUMN_NAME = "name";

        public static final Field FIELD_NAME = TABLE.field(COLUMN_NAME);

        static final String[] PROJECTION_ALL = {COLUMN_NAME};

        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_PRIMARY_KEY = uniqueIndex(COLUMN_NAME);

        public static final Join<LocalFile, Attachment> SQL_JOIN_ATTACHMENT =
                TABLE.join(AttachmentTable.TABLE).on(FIELD_NAME.eq(AttachmentTable.FIELD_LOCALFILENAME));
        public static final Join<LocalFile, TranslationFile> SQL_JOIN_TRANSLATION_FILE =
                TABLE.join(TranslationFileTable.TABLE).on(FIELD_NAME.eq(TranslationFileTable.FIELD_FILE));

        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_NAME.eq(bind());

        static final String SQL_CREATE_TABLE = create(TABLE_NAME, SQL_COLUMN_NAME, SQL_PRIMARY_KEY);
        static final String SQL_DELETE_TABLE = drop(TABLE_NAME);
    }

    public static class TranslationFileTable implements Base {
        static final String TABLE_NAME = "translation_files";
        static final Table<TranslationFile> TABLE = Table.forClass(TranslationFile.class);

        static final String COLUMN_TRANSLATION = "translation";
        static final String COLUMN_FILE = "file";

        private static final Field FIELD_TRANSLATION = TABLE.field(COLUMN_TRANSLATION);
        public static final Field FIELD_FILE = TABLE.field(COLUMN_FILE);

        static final String[] PROJECTION_ALL = {COLUMN_TRANSLATION, COLUMN_FILE};

        private static final String SQL_COLUMN_TRANSLATION = COLUMN_TRANSLATION + " INTEGER NOT NULL";
        private static final String SQL_COLUMN_NAME = COLUMN_FILE + " TEXT NOT NULL";
        private static final String SQL_PRIMARY_KEY = uniqueIndex(COLUMN_TRANSLATION, COLUMN_FILE);

        public static final Join<TranslationFile, Translation> SQL_JOIN_TRANSLATION =
                TABLE.join(TranslationTable.TABLE).on(FIELD_TRANSLATION.eq(TranslationTable.FIELD_ID));

        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_TRANSLATION.eq(bind()).and(FIELD_FILE.eq(bind()));

        static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_TRANSLATION, SQL_COLUMN_NAME, SQL_PRIMARY_KEY);
        static final String SQL_DELETE_TABLE = drop(TABLE_NAME);
    }

    public static class AttachmentTable extends BaseTable implements ToolId {
        public static final String TABLE_NAME = "attachments";
        static final Table<Attachment> TABLE = Table.forClass(Attachment.class);

        public static final String COLUMN_FILENAME = "filename";
        public static final String COLUMN_SHA256 = "sha256";
        public static final String COLUMN_LOCALFILENAME = "local_filename";
        public static final String COLUMN_DOWNLOADED = "downloaded";

        public static final Field FIELD_ID = TABLE.field(COLUMN_ID);
        public static final Field FIELD_TOOL = TABLE.field(COLUMN_TOOL);
        static final Field FIELD_LOCALFILENAME = TABLE.field(COLUMN_LOCALFILENAME);
        public static final Field FIELD_DOWNLOADED = TABLE.field(COLUMN_DOWNLOADED);

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_TOOL, COLUMN_FILENAME, COLUMN_SHA256, COLUMN_LOCALFILENAME, COLUMN_DOWNLOADED};

        private static final String SQL_COLUMN_FILENAME = COLUMN_FILENAME + " TEXT";
        private static final String SQL_COLUMN_SHA256 = COLUMN_SHA256 + " TEXT";
        private static final String SQL_COLUMN_LOCALFILENAME = COLUMN_LOCALFILENAME + " TEXT";
        private static final String SQL_COLUMN_DOWNLOADED = COLUMN_DOWNLOADED + " INTEGER";

        public static final Join<Attachment, Tool> SQL_JOIN_TOOL =
                TABLE.join(ToolTable.TABLE).on(FIELD_TOOL.eq(ToolTable.FIELD_ID));
        public static final Join<Attachment, LocalFile> SQL_JOIN_LOCAL_FILE =
                TABLE.join(LocalFileTable.TABLE).on(FIELD_LOCALFILENAME.eq(LocalFileTable.FIELD_NAME));

        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_ID.eq(bind());
        public static final Expression SQL_WHERE_DOWNLOADED = FIELD_DOWNLOADED.eq(true);

        static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ID, SQL_COLUMN_TOOL, SQL_COLUMN_FILENAME, SQL_COLUMN_SHA256,
                       SQL_COLUMN_LOCALFILENAME, SQL_COLUMN_DOWNLOADED);
        static final String SQL_DELETE_TABLE = drop(TABLE_NAME);
    }

    static class FollowupTable extends BaseTable {
        static final String TABLE_NAME = "followups";
        private static final Table<Followup> TABLE = Table.forClass(Followup.class);

        static final String COLUMN_NAME = "name";
        static final String COLUMN_EMAIL = "email";
        static final String COLUMN_DESTINATION = "destination";
        static final String COLUMN_LANGUAGE = "language";
        static final String COLUMN_CREATE_TIME = "created_at";

        private static final Field FIELD_ID = TABLE.field(COLUMN_ID);

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_DESTINATION, COLUMN_LANGUAGE};

        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_EMAIL = COLUMN_EMAIL + " TEXT";
        private static final String SQL_COLUMN_DESTINATION = COLUMN_DESTINATION + " INTEGER";
        private static final String SQL_COLUMN_LANGUAGE = COLUMN_LANGUAGE + " TEXT NOT NULL";
        private static final String SQL_COLUMN_CREATE_TIME = COLUMN_CREATE_TIME + " INTEGER";

        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_ID.eq(bind());

        static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ID, SQL_COLUMN_NAME, SQL_COLUMN_EMAIL, SQL_COLUMN_DESTINATION,
                       SQL_COLUMN_LANGUAGE, SQL_COLUMN_CREATE_TIME);
        static final String SQL_DELETE_TABLE = drop(TABLE_NAME);
    }

    static class GlobalActivityAnalyticsTable extends BaseTable {
        static final String TABLE_NAME = "global_activity_analytics";
        private static final Table<GlobalActivityAnalytics> TABLE = Table.forClass(GlobalActivityAnalytics.class);

        static final String COLUMN_USERS = "users";
        static final String COLUMN_COUNTRIES = "countries";
        static final String COLUMN_LAUNCHES = "launches";
        static final String COLUMN_GOSPEL_PRESENTATIONS = "gospel_presentations";

        private static final Field FIELD_ID = TABLE.field(COLUMN_ID);

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_USERS, COLUMN_COUNTRIES, COLUMN_LAUNCHES, COLUMN_GOSPEL_PRESENTATIONS};

        private static final String SQL_COLUMN_USERS = COLUMN_USERS + " INTEGER";
        private static final String SQL_COLUMN_COUNTRIES = COLUMN_COUNTRIES + " INTEGER";
        private static final String SQL_COLUMN_LAUNCHES = COLUMN_LAUNCHES + " INTEGER";
        private static final String SQL_COLUMN_GOSPEL_PRESENTATIONS = COLUMN_GOSPEL_PRESENTATIONS + " INTEGER";

        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_ID.eq(bind());

        static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ID, SQL_COLUMN_USERS, SQL_COLUMN_COUNTRIES, SQL_COLUMN_LAUNCHES,
                       SQL_COLUMN_GOSPEL_PRESENTATIONS);
        static final String SQL_DELETE_TABLE = drop(TABLE_NAME);

        // region DB Migrations
        static final String SQL_V41_CREATE_GLOBAL_ANALYTICS =
                create(TABLE_NAME, SQL_COLUMN_ID, SQL_COLUMN_USERS, SQL_COLUMN_COUNTRIES, SQL_COLUMN_LAUNCHES,
                       SQL_COLUMN_GOSPEL_PRESENTATIONS);
        // endregion DB Migrations
    }
}
