package org.keynote.godtools.android.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Pair;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.db.StreamDao;
import org.ccci.gto.android.common.db.Transaction;
import org.ccci.gto.android.common.db.async.AbstractAsyncDao;
import org.ccci.gto.android.common.util.ArrayUtils;
import org.cru.godtools.model.Attachment;
import org.cru.godtools.model.Base;
import org.cru.godtools.model.Followup;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.LocalFile;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.TranslationFile;
import org.keynote.godtools.android.db.Contract.AttachmentTable;
import org.keynote.godtools.android.db.Contract.FollowupTable;
import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.db.Contract.LocalFileTable;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.Contract.TranslationFileTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;

import java.util.Locale;

public class GodToolsDao extends AbstractAsyncDao implements StreamDao {
    private GodToolsDao(@NonNull final Context context) {
        super(GodToolsDatabase.getInstance(context));

        registerType(Followup.class, FollowupTable.TABLE_NAME, FollowupTable.PROJECTION_ALL, new FollowupMapper(),
                     FollowupTable.SQL_WHERE_PRIMARY_KEY);
        registerType(Language.class, LanguageTable.TABLE_NAME, LanguageTable.PROJECTION_ALL, new LanguageMapper(),
                     LanguageTable.SQL_WHERE_PRIMARY_KEY);
        registerType(Tool.class, ToolTable.TABLE_NAME, ToolTable.PROJECTION_ALL, new ToolMapper(),
                     ToolTable.SQL_WHERE_PRIMARY_KEY);
        registerType(Attachment.class, AttachmentTable.TABLE_NAME, AttachmentTable.PROJECTION_ALL,
                     new AttachmentMapper(), AttachmentTable.SQL_WHERE_PRIMARY_KEY);
        registerType(Translation.class, TranslationTable.TABLE_NAME, TranslationTable.PROJECTION_ALL,
                     new TranslationMapper(), TranslationTable.SQL_WHERE_PRIMARY_KEY);
        registerType(LocalFile.class, LocalFileTable.TABLE_NAME, LocalFileTable.PROJECTION_ALL, new LocalFileMapper(),
                     LocalFileTable.SQL_WHERE_PRIMARY_KEY);
        registerType(TranslationFile.class, TranslationFileTable.TABLE_NAME, TranslationFileTable.PROJECTION_ALL,
                     new TranslationFileMapper(), TranslationFileTable.SQL_WHERE_PRIMARY_KEY);
    }

    @Nullable
    private static GodToolsDao sInstance;
    @NonNull
    public static GodToolsDao getInstance(@NonNull final Context context) {
        synchronized (GodToolsDao.class) {
            if (sInstance == null) {
                sInstance = new GodToolsDao(context.getApplicationContext());
            }
        }

        return sInstance;
    }

    @NonNull
    @Override
    public Expression getPrimaryKeyWhere(@NonNull final Object obj) {
        if (obj instanceof LocalFile) {
            return getPrimaryKeyWhere(LocalFile.class, ((LocalFile) obj).getFileName());
        } else if (obj instanceof TranslationFile) {
            final TranslationFile file = (TranslationFile) obj;
            return getPrimaryKeyWhere(TranslationFile.class, file.getTranslationId(), file.getFileName());
        } else if (obj instanceof Language) {
            return getPrimaryKeyWhere(Language.class, ((Language) obj).getCode());
        } else if (obj instanceof Tool) {
            return getPrimaryKeyWhere(Tool.class, ((Tool) obj).getCode());
        } else if (obj instanceof Base) {
            return getPrimaryKeyWhere(obj.getClass(), ((Base) obj).getId());
        }

        return super.getPrimaryKeyWhere(obj);
    }

    @NonNull
    @Override
    @WorkerThread
    public <T> Stream<T> streamCompat(@NonNull final Query<T> query) {
        return StreamHelper.stream(this, query);
    }

    /* Miscellaneous app specific dao methods */

    @WorkerThread
    public long insertNew(final Base obj) {
        int attempts = 10;
        while (true) {
            obj.initNew();
            try {
                return insert(obj, SQLiteDatabase.CONFLICT_ABORT);
            } catch (final SQLException e) {
                // propagate exception if we've exhausted our attempts
                if (--attempts < 0) {
                    throw e;
                }
            }
        }
    }

    @WorkerThread
    public void updateSharesDelta(@Nullable final String toolCode, final int shares) {
        // short-circuit if this is a valid tool
        if (toolCode == null) {
            return;
        }

        // short-circuit if the delta isn't actually changing
        if (shares == 0) {
            return;
        }

        // build update query
        final Pair<String, String[]> where = compileExpression(getPrimaryKeyWhere(Tool.class, toolCode));
        final StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTable(Tool.class))
                .append(" SET " + ToolTable.COLUMN_PENDING_SHARES +
                                " = coalesce(" + ToolTable.COLUMN_PENDING_SHARES + ", 0) + ?")
                .append(" WHERE ").append(where.first);
        final String[] args = ArrayUtils.merge(String.class, bindValues(shares), where.second);

        // perform update and sanitize PersonalMeasurements
        final SQLiteDatabase db = getWritableDatabase();
        final Transaction tx = newTransaction(db);
        try {
            tx.beginTransactionNonExclusive();
            db.execSQL(sql.toString(), args);
            tx.setTransactionSuccessful();
        } finally {
            tx.endTransaction().recycle();
        }
    }

    @NonNull
    @WorkerThread
    public Optional<Translation> getLatestTranslation(@Nullable final String code, @Nullable final Locale locale) {
        if (code == null || locale == null) {
            return Optional.empty();
        }

        return streamCompat(Query.select(Translation.class)
                                    .where(TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(code, locale))
                                    .orderBy(TranslationTable.SQL_ORDER_BY_VERSION_DESC)
                                    .limit(1))
                .findFirst();
    }
}
