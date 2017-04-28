package org.keynote.godtools.android.db;

import org.ccci.gto.android.common.db.BaseContract;
import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Expression.Field;
import org.ccci.gto.android.common.db.Table;
import org.keynote.godtools.android.model.Followup;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.model.Resource;
import org.keynote.godtools.android.model.Translation;

import static org.ccci.gto.android.common.db.Expression.bind;

public final class Contract extends BaseContract {
    public abstract static class BaseTable implements Base {
        public static final String COLUMN_ID = COLUMN_ROWID;
        static final String SQL_COLUMN_ID = SQL_COLUMN_ROWID;
    }

    public static class LanguageTable extends BaseTable {
        static final String TABLE_NAME = "languages";
        private static final Table<Language> TABLE = Table.forClass(Language.class);

        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_ADDED = "added";

        public static final Field FIELD_CODE = TABLE.field(COLUMN_CODE);

        static final String[] PROJECTION_ALL = {COLUMN_ID, COLUMN_CODE, COLUMN_ADDED};

        private static final String SQL_COLUMN_CODE = COLUMN_CODE + " TEXT NOT NULL";
        private static final String SQL_COLUMN_ADDED = COLUMN_ADDED + " INTEGER";
        private static final String SQL_PRIMARY_KEY = uniqueIndex(COLUMN_CODE);

        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_CODE.eq(bind());

        static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ROWID, SQL_COLUMN_CODE, SQL_COLUMN_ADDED, SQL_PRIMARY_KEY);
        static final String SQL_DELETE_TABLE = drop(TABLE_NAME);
    }

    public static class ResourceTable extends BaseTable {
        static final String TABLE_NAME = "resources";
        private static final Table<Resource> TABLE = Table.forClass(Resource.class);

        public static final String COLUMN_NAME = "name";
        static final String COLUMN_ADDED = "added";

        private static final Field FIELD_ID = TABLE.field(COLUMN_ID);

        static final String[] PROJECTION_ALL = {COLUMN_ID, COLUMN_NAME, COLUMN_ADDED};

        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_ADDED = COLUMN_ADDED + " INTEGER";

        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_ID.eq(bind());

        static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ID, SQL_COLUMN_NAME, SQL_COLUMN_ADDED);
        static final String SQL_DELETE_TABLE = drop(TABLE_NAME);
    }

    public static class TranslationTable extends BaseTable {
        static final String TABLE_NAME = "translations";
        private static final Table<Translation> TABLE = Table.forClass(Translation.class);

        public static final String COLUMN_RESOURCE = "resource";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_VERSION = "version";
        public static final String COLUMN_PUBLISHED = "published";
        static final String COLUMN_DOWNLOADED = "downloaded";

        private static final Field FIELD_ID = TABLE.field(COLUMN_ID);

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_RESOURCE, COLUMN_LANGUAGE, COLUMN_VERSION, COLUMN_PUBLISHED, COLUMN_DOWNLOADED};

        private static final String SQL_COLUMN_RESOURCE = COLUMN_RESOURCE + " INTEGER";
        private static final String SQL_COLUMN_LANGUAGE = COLUMN_LANGUAGE + " INTEGER";
        private static final String SQL_COLUMN_VERSION = COLUMN_VERSION + " INTEGER";
        private static final String SQL_COLUMN_PUBLISHED = COLUMN_PUBLISHED + " INTEGER";
        private static final String SQL_COLUMN_DOWNLOADED = COLUMN_DOWNLOADED + " INTEGER";

        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_ID.eq(bind());

        static final String SQL_CREATE_TABLE =
                create(TABLE_NAME, SQL_COLUMN_ID, SQL_COLUMN_RESOURCE, SQL_COLUMN_LANGUAGE, SQL_COLUMN_VERSION,
                       SQL_COLUMN_PUBLISHED, SQL_COLUMN_DOWNLOADED);
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
