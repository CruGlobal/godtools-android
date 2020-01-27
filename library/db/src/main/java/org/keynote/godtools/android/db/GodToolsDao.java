package org.keynote.godtools.android.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.annimon.stream.LongStream;
import com.annimon.stream.Optional;

import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.util.ArrayUtils;
import org.cru.godtools.model.Base;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

public class GodToolsDao extends GodToolsDaoKotlin {
    private GodToolsDao(@NonNull final Context context) {
        super(context);
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
        inTransaction(db, false, () -> {
            db.execSQL(sql.toString(), args);
            invalidateClass(Tool.class);
            return null;
        });
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

    @WorkerThread
    public void updateToolOrder(final long... tools) {
        final Tool tool = new Tool();

        inNonExclusiveTransaction(() -> {
            // reset order for all tools
            update(tool, (Expression) null, ToolTable.COLUMN_ORDER);

            // set order for each specified tool
            LongStream.of(tools).boxed().indexed()
                    .forEach(t -> {
                        tool.setOrder(t.getFirst());
                        update(tool, ToolTable.FIELD_ID.eq(t.getSecond()), ToolTable.COLUMN_ORDER);
                    });
            return null;
        });
    }
}
