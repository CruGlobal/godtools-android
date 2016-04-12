package org.keynote.godtools.android.dao;

import android.text.TextUtils;

import org.ccci.gto.android.common.db.BaseContract;
import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Expression.Field;
import org.ccci.gto.android.common.db.Table;
import org.keynote.godtools.android.business.GSSubscriber;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.model.Followup;

import static org.ccci.gto.android.common.db.Expression.bind;
import static org.ccci.gto.android.common.db.Expression.field;

public class DBContract extends BaseContract {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String COMMA_SEP = ",";

    public static abstract class GTPackageTable implements Base {
        public static final String TABLE_NAME = "gtpackages";
        public static final Table<GTPackage> TABLE = Table.forClass(GTPackage.class);

        public static final String COL_LANGUAGE = "language";
        public static final String COL_STATUS = "status";
        public static final String COL_CODE = "code";
        public static final String COL_NAME = "name";
        public static final String COL_VERSION = "version";
        public static final String COL_CONFIG_FILE_NAME = "config_file_name";
        public static final String COL_ICON = "icon";

        public static final Field FIELD_LANGUAGE = field(TABLE, COL_LANGUAGE);
        public static final Field FIELD_STATUS = field(TABLE, COL_STATUS);
        public static final Field FIELD_CODE = field(TABLE, COL_CODE);

        public static final String[] PROJECTION_ALL =
                {COL_LANGUAGE, COL_STATUS, COL_CODE, COL_NAME, COL_CONFIG_FILE_NAME, COL_ICON, COL_VERSION};

        private static final String SQL_COLUMN_LANGUAGE = COL_LANGUAGE + " TEXT NOT NULL DEFAULT ''";
        private static final String SQL_COLUMN_STATUS = COL_STATUS + " TEXT NOT NULL DEFAULT ''";
        private static final String SQL_COLUMN_CODE = COL_CODE + " TEXT NOT NULL DEFAULT ''";
        private static final String SQL_COLUMN_NAME = COL_NAME + TEXT_TYPE;
        private static final String SQL_COLUMN_CONFIG_FILE_NAME = COL_CONFIG_FILE_NAME + TEXT_TYPE;
        private static final String SQL_COLUMN_ICON = COL_ICON + TEXT_TYPE;
        private static final String SQL_COLUMN_VERSION = COL_VERSION + " TEXT";
        private static final String SQL_PRIMARY_KEY =
                "UNIQUE(" + COL_LANGUAGE + "," + COL_STATUS + "," + COL_CODE + ")";

        static final Expression SQL_WHERE_PRIMARY_KEY =
                FIELD_LANGUAGE.eq(bind()).and(FIELD_STATUS.eq(bind())).and(FIELD_CODE.eq(bind()));
        public static final Expression SQL_WHERE_DRAFT_BY_LANGUAGE =
                FIELD_LANGUAGE.eq(bind()).and(FIELD_STATUS.eq(GTPackage.STATUS_DRAFT));

        public static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ROWID, SQL_COLUMN_CODE, SQL_COLUMN_NAME, SQL_COLUMN_LANGUAGE,
                       SQL_COLUMN_CONFIG_FILE_NAME, SQL_COLUMN_ICON, SQL_COLUMN_STATUS, SQL_COLUMN_VERSION,
                       SQL_PRIMARY_KEY);
        public static final String SQL_DELETE_TABLE = drop(TABLE_NAME);

        // migration db queries
        public static final String OLD_TABLE_NAME = "gtpackages_old";
        static final String SQL_RENAME_TABLE = "ALTER TABLE " + TABLE_NAME + " RENAME TO " + OLD_TABLE_NAME;
        static final String SQL_DELETE_OLD_TABLE = drop(OLD_TABLE_NAME);
        @Deprecated
        static final String SQL_V2_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ROWID, SQL_COLUMN_CODE, SQL_COLUMN_NAME, SQL_COLUMN_LANGUAGE,
                       SQL_COLUMN_CONFIG_FILE_NAME, SQL_COLUMN_ICON, SQL_COLUMN_STATUS, SQL_COLUMN_VERSION);
        @Deprecated
        private static final String SQL_V3_MIGRATE_COLUMNS = TextUtils.join(",", new Object[] {COL_LANGUAGE, COL_STATUS,
                COL_CODE, COL_NAME, COL_VERSION, COL_CONFIG_FILE_NAME, COL_ICON});
        @Deprecated
        static final String SQL_V3_MIGRATE_DATA =
                "INSERT OR IGNORE INTO " + TABLE_NAME + " (" + SQL_V3_MIGRATE_COLUMNS + ") SELECT " +
                        SQL_V3_MIGRATE_COLUMNS + " FROM " + OLD_TABLE_NAME;
    }

    public static abstract class GTLanguageTable implements Base {
        public static final String TABLE_NAME = "gtlanguages";
        public static final Table<GTLanguage> TABLE = Table.forClass(GTLanguage.class);

        public static final String COL_CODE = "code";
        public static final String COL_NAME = "name";
        public static final String COL_DOWNLOADED = "is_downloaded";
        public static final String COL_DRAFT = "is_draft";

        static final String[] PROJECTION_ALL = {COL_CODE, COL_NAME, COL_DOWNLOADED, COL_DRAFT};

        private static final String SQL_COLUMN_CODE = COL_CODE + " TEXT NOT NULL";
        private static final String SQL_COLUMN_NAME = COL_NAME + " TEXT";
        private static final String SQL_COLUMN_DOWNLOADED = COL_DOWNLOADED + " INTEGER";
        private static final String SQL_COLUMN_DRAFT = COL_DRAFT + " INTEGER";
        private static final String SQL_PRIMARY_KEY = uniqueIndex(COL_CODE);

        static final Expression SQL_WHERE_PRIMARY_KEY = TABLE.field(COL_CODE).eq(bind());

        static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ROWID, SQL_COLUMN_CODE, SQL_COLUMN_DOWNLOADED, SQL_COLUMN_DRAFT,
                       SQL_COLUMN_NAME, SQL_PRIMARY_KEY);
        static final String SQL_DELETE_TABLE = drop(TABLE_NAME);

        // migration db queries
        static final String OLD_TABLE_NAME = "gtlanguages_old";
        static final String SQL_RENAME_TABLE = "ALTER TABLE " + TABLE_NAME + " RENAME TO " + OLD_TABLE_NAME;
        static final String SQL_DELETE_OLD_TABLE = drop(OLD_TABLE_NAME);

        @Deprecated
        static final String SQL_V1_MIGRATE_DATA = "INSERT INTO " + TABLE_NAME
                + " (" + GTLanguageTable._ID + COMMA_SEP
                + GTLanguageTable.COL_CODE + COMMA_SEP
                + GTLanguageTable.COL_DOWNLOADED + COMMA_SEP
                + GTLanguageTable.COL_DRAFT + ")" +
                " SELECT * FROM " + OLD_TABLE_NAME;
        @Deprecated
        static final String SQL_V2_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ROWID, SQL_COLUMN_CODE, SQL_COLUMN_DOWNLOADED, SQL_COLUMN_DRAFT,
                       SQL_COLUMN_NAME);
        @Deprecated
        private static final String SQL_V6_MIGRATE_COLUMNS = TextUtils.join(",", new Object[] {COL_CODE, COL_DOWNLOADED,
                COL_DRAFT, COL_DOWNLOADED});
        @Deprecated
        static final String SQL_V6_MIGRATE_DATA =
                "INSERT OR IGNORE INTO " + TABLE_NAME + " (" + SQL_V6_MIGRATE_COLUMNS + ") SELECT " +
                        SQL_V6_MIGRATE_COLUMNS + " FROM " + OLD_TABLE_NAME;
    }

    /*Growth Spaces subscriber table*/
    public static abstract class GSSubscriberTable implements Base {
        public static final String TABLE_NAME = "gssubscribers";
        public static final Table<GSSubscriber> TABLE = Table.forClass(GSSubscriber.class);

        public static final String COLUMN_SUBSCRIBER_ID = _ID;
        public static final String COL_ROUTE_ID = "route_id";
        public static final String COL_LANGUAGE_CODE = "language_code";
        public static final String COL_FIRST_NAME = "first_name";
        public static final String COL_LAST_NAME = "last_name";
        public static final String COL_EMAIL = "email";
        public static final String COL_CREATED_TIMESTAMP = "created_timestamp";

        public static final Field FIELD_SUBSCRIBER_ID = TABLE.field(COLUMN_SUBSCRIBER_ID);

        public static final String[] PROJECTION_ALL =
                {COLUMN_SUBSCRIBER_ID, COL_ROUTE_ID, COL_LANGUAGE_CODE, COL_FIRST_NAME, COL_LAST_NAME, COL_EMAIL,
                        COL_CREATED_TIMESTAMP};

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + GSSubscriberTable.TABLE_NAME + "("
                + GSSubscriberTable._ID + INTEGER_TYPE + PRIMARY_KEY + COMMA_SEP
                + GSSubscriberTable.COL_ROUTE_ID + INTEGER_TYPE + COMMA_SEP
                + GSSubscriberTable.COL_LANGUAGE_CODE + TEXT_TYPE + COMMA_SEP
                + GSSubscriberTable.COL_FIRST_NAME + TEXT_TYPE + COMMA_SEP
                + GSSubscriberTable.COL_LAST_NAME + TEXT_TYPE + COMMA_SEP
                + GSSubscriberTable.COL_EMAIL + TEXT_TYPE + COMMA_SEP
                + GSSubscriberTable.COL_CREATED_TIMESTAMP + INTEGER_TYPE + ")";

        public static final String SQL_DELETE_TABLE = drop(TABLE_NAME);

        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_SUBSCRIBER_ID.eq(bind());
    }

    public static class FollowupTable implements Base {
        public static final String TABLE_NAME = "followups";
        public static final Table<Followup> TABLE = Table.forClass(Followup.class);

        public static final String COLUMN_ID = "followup_id";
        public static final String COLUMN_CONTEXT_ID = "context_id";
        public static final String COLUMN_GS_ROUTE_ID = "gs_route_id";
        public static final String COLUMN_GS_ACCESS_ID = "gs_access_id";
        public static final String COLUMN_GS_ACCESS_SECRET = "gs_access_secret";

        public static final Field FIELD_GS_ROUTE_ID = TABLE.field(COLUMN_GS_ROUTE_ID);

        public static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_CONTEXT_ID, COLUMN_GS_ROUTE_ID, COLUMN_GS_ACCESS_ID,
                        COLUMN_GS_ACCESS_SECRET};

        private static final String SQL_COLUMN_ID = COLUMN_ID + " INTEGER NOT NULL";
        private static final String SQL_COLUMN_CONTEXT_ID = COLUMN_CONTEXT_ID + " INTEGER NOT NULL";
        private static final String SQL_COLUMN_GS_ROUTE_ID = COLUMN_GS_ROUTE_ID + " INTEGER";
        private static final String SQL_COLUMN_GS_ACCESS_ID = COLUMN_GS_ACCESS_ID + " TEXT";
        private static final String SQL_COLUMN_GS_ACCESS_SECRET = COLUMN_GS_ACCESS_SECRET + " TEXT";
        private static final String SQL_PRIMARY_KEY = uniqueIndex(COLUMN_ID, COLUMN_CONTEXT_ID);

        public static final Expression SQL_WHERE_PRIMARY_KEY =
                TABLE.field(COLUMN_ID).eq(bind()).and(TABLE.field(COLUMN_CONTEXT_ID).eq(bind()));

        public static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ROWID, SQL_COLUMN_ID, SQL_COLUMN_CONTEXT_ID, SQL_COLUMN_GS_ROUTE_ID,
                       SQL_COLUMN_GS_ACCESS_ID, SQL_COLUMN_GS_ACCESS_SECRET, SQL_PRIMARY_KEY);
        public static final String SQL_DELETE_TABLE = drop(TABLE_NAME);
    }
}
