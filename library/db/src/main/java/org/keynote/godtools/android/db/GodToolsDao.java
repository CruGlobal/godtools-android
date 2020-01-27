package org.keynote.godtools.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import org.ccci.gto.android.common.util.ArrayUtils;
import org.cru.godtools.model.Tool;
import org.keynote.godtools.android.db.Contract.ToolTable;

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
}
