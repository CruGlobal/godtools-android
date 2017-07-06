package org.keynote.godtools.android.dao;

import android.text.TextUtils;

import org.ccci.gto.android.common.db.BaseContract;
import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Expression.Field;
import org.ccci.gto.android.common.db.Table;
import org.keynote.godtools.android.business.GTPackage;

import static org.ccci.gto.android.common.db.Expression.bind;
import static org.ccci.gto.android.common.db.Expression.field;

public class DBContract extends BaseContract {
    private static final String TEXT_TYPE = " TEXT";

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

        public static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ROWID, SQL_COLUMN_CODE, SQL_COLUMN_NAME, SQL_COLUMN_LANGUAGE,
                       SQL_COLUMN_CONFIG_FILE_NAME, SQL_COLUMN_ICON, SQL_COLUMN_STATUS, SQL_COLUMN_VERSION,
                       SQL_PRIMARY_KEY);
        public static final String SQL_DELETE_TABLE = drop(TABLE_NAME);

        // migration db queries
        public static final String OLD_TABLE_NAME = "gtpackages_old";
        public static final String SQL_RENAME_TABLE = "ALTER TABLE " + TABLE_NAME + " RENAME TO " + OLD_TABLE_NAME;
        public static final String SQL_DELETE_OLD_TABLE = drop(OLD_TABLE_NAME);
        @Deprecated
        public static final String SQL_V2_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ROWID, SQL_COLUMN_CODE, SQL_COLUMN_NAME, SQL_COLUMN_LANGUAGE,
                       SQL_COLUMN_CONFIG_FILE_NAME, SQL_COLUMN_ICON, SQL_COLUMN_STATUS, SQL_COLUMN_VERSION);
        @Deprecated
        private static final String SQL_V3_MIGRATE_COLUMNS = TextUtils.join(",", new Object[] {COL_LANGUAGE, COL_STATUS,
                COL_CODE, COL_NAME, COL_VERSION, COL_CONFIG_FILE_NAME, COL_ICON});
        @Deprecated
        public static final String SQL_V3_MIGRATE_DATA =
                "INSERT OR IGNORE INTO " + TABLE_NAME + " (" + SQL_V3_MIGRATE_COLUMNS + ") SELECT " +
                        SQL_V3_MIGRATE_COLUMNS + " FROM " + OLD_TABLE_NAME;
    }
}
