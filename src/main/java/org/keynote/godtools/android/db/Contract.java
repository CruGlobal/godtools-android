package org.keynote.godtools.android.db;

import org.ccci.gto.android.common.db.BaseContract;
import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Expression.Field;
import org.ccci.gto.android.common.db.Table;
import org.keynote.godtools.android.model.Followup;
import org.keynote.godtools.android.model.Language;

import static org.ccci.gto.android.common.db.Expression.bind;

public final class Contract extends BaseContract {
    public abstract static class BaseTable implements Base {
        public static final String COLUMN_ID = COLUMN_ROWID;
        static final String SQL_COLUMN_ID = SQL_COLUMN_ROWID;
    }

    public static class LanguageTable extends BaseTable {
        static final String TABLE_NAME = "languages";
        private static final Table<Language> TABLE = Table.forClass(Language.class);

        private static final Field FIELD_ID = TABLE.field(COLUMN_ID);
        static final String COLUMN_LOCALE = "locale";

        public static final Field FIELD_LOCALE = TABLE.field(COLUMN_LOCALE);

        static final String[] PROJECTION_ALL = {COLUMN_ID, COLUMN_LOCALE};

        private static final String SQL_COLUMN_LOCALE = COLUMN_LOCALE + " TEXT";

        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_ID.eq(bind());

        static final String SQL_CREATE_TABLE = create(TABLE_NAME, SQL_COLUMN_ID, SQL_COLUMN_LOCALE);
        static final String SQL_DELETE_TABLE = drop(TABLE_NAME);
    }

    public static class FollowupTable implements Base {
        static final String TABLE_NAME = "followups";
        private static final Table<Followup> TABLE = Table.forClass(Followup.class);

        static final String COLUMN_ID = "followup_id";
        static final String COLUMN_CONTEXT_ID = "context_id";
        static final String COLUMN_GS_ROUTE_ID = "gs_route_id";
        static final String COLUMN_GS_ACCESS_ID = "gs_access_id";
        static final String COLUMN_GS_ACCESS_SECRET = "gs_access_secret";

        private static final Field FIELD_ID = TABLE.field(COLUMN_ID);
        private static final Field FIELD_CONTEXT_ID = TABLE.field(COLUMN_CONTEXT_ID);
        public static final Field FIELD_GS_ROUTE_ID = TABLE.field(COLUMN_GS_ROUTE_ID);

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_CONTEXT_ID, COLUMN_GS_ROUTE_ID, COLUMN_GS_ACCESS_ID, COLUMN_GS_ACCESS_SECRET};

        private static final String SQL_COLUMN_ID = COLUMN_ID + " INTEGER NOT NULL";
        private static final String SQL_COLUMN_CONTEXT_ID = COLUMN_CONTEXT_ID + " INTEGER NOT NULL";
        private static final String SQL_COLUMN_GS_ROUTE_ID = COLUMN_GS_ROUTE_ID + " INTEGER";
        private static final String SQL_COLUMN_GS_ACCESS_ID = COLUMN_GS_ACCESS_ID + " TEXT";
        private static final String SQL_COLUMN_GS_ACCESS_SECRET = COLUMN_GS_ACCESS_SECRET + " TEXT";
        private static final String SQL_PRIMARY_KEY = uniqueIndex(COLUMN_ID, COLUMN_CONTEXT_ID);

        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_ID.eq(bind()).and(FIELD_CONTEXT_ID.eq(bind()));

        static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ROWID, SQL_COLUMN_ID, SQL_COLUMN_CONTEXT_ID, SQL_COLUMN_GS_ROUTE_ID,
                       SQL_COLUMN_GS_ACCESS_ID, SQL_COLUMN_GS_ACCESS_SECRET, SQL_PRIMARY_KEY);
        static final String SQL_DELETE_TABLE = drop(TABLE_NAME);
    }
}
