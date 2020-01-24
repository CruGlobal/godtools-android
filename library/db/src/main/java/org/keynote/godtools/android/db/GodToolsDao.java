package org.keynote.godtools.android.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.annimon.stream.LongStream;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.LiveDataDao;
import org.ccci.gto.android.common.db.LiveDataRegistry;
import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.db.StreamDao;
import org.ccci.gto.android.common.db.async.AbstractAsyncDao;
import org.ccci.gto.android.common.util.ArrayUtils;
import org.cru.godtools.model.Attachment;
import org.cru.godtools.model.Base;
import org.cru.godtools.model.Followup;
import org.cru.godtools.model.GlobalActivityAnalytics;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.LocalFile;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.TranslationFile;
import org.keynote.godtools.android.db.Contract.AttachmentTable;
import org.keynote.godtools.android.db.Contract.FollowupTable;
import org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable;
import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.db.Contract.LocalFileTable;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.Contract.TranslationFileTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

public class GodToolsDao extends AbstractAsyncDao implements LiveDataDao, StreamDao {
    private GodToolsDao(@NonNull final Context context) {
        super(GodToolsDatabase.getInstance(context));

        registerType(Followup.class, FollowupTable.TABLE_NAME, FollowupTable.PROJECTION_ALL, new FollowupMapper(),
                     FollowupTable.SQL_WHERE_PRIMARY_KEY);
        registerType(Language.class, LanguageTable.TABLE_NAME, LanguageTable.PROJECTION_ALL, LanguageMapper.INSTANCE,
                     LanguageTable.SQL_WHERE_PRIMARY_KEY);
        registerType(Tool.class, ToolTable.TABLE_NAME, ToolTable.PROJECTION_ALL, new ToolMapper(),
                     ToolTable.SQL_WHERE_PRIMARY_KEY);
        registerType(Attachment.class, AttachmentTable.TABLE_NAME, AttachmentTable.PROJECTION_ALL,
                     new AttachmentMapper(), AttachmentTable.SQL_WHERE_PRIMARY_KEY);
        registerType(Translation.class, TranslationTable.TABLE_NAME, TranslationTable.PROJECTION_ALL,
                     new TranslationMapper(), TranslationTable.SQL_WHERE_PRIMARY_KEY);
        registerType(LocalFile.class, LocalFileTable.TABLE_NAME, LocalFileTable.PROJECTION_ALL,
                     LocalFileMapper.INSTANCE, LocalFileTable.SQL_WHERE_PRIMARY_KEY);
        registerType(TranslationFile.class, TranslationFileTable.TABLE_NAME, TranslationFileTable.PROJECTION_ALL,
                     new TranslationFileMapper(), TranslationFileTable.SQL_WHERE_PRIMARY_KEY);
        registerType(GlobalActivityAnalytics.class, GlobalActivityAnalyticsTable.TABLE_NAME,
                     GlobalActivityAnalyticsTable.PROJECTION_ALL, GlobalActivityAnalyticsMapper.INSTANCE,
                     GlobalActivityAnalyticsTable.SQL_WHERE_PRIMARY_KEY);
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
        } else if (obj instanceof GlobalActivityAnalytics) {
            return getPrimaryKeyWhere(obj.getClass(), ((GlobalActivityAnalytics) obj).getId());
        } else if (obj instanceof Base) {
            return getPrimaryKeyWhere(obj.getClass(), ((Base) obj).getId());
        }
        return super.getPrimaryKeyWhere(obj);
    }

    @Override
    protected void onInvalidateClass(@NonNull final Class<?> clazz) {
        super.onInvalidateClass(clazz);
        getLiveDataRegistry().invalidate(clazz);
    }

    // region LiveDataDao
    private final LiveDataRegistry mLiveDataRegistry = new LiveDataRegistry();

    @NonNull
    @Override
    public LiveDataRegistry getLiveDataRegistry() {
        return mLiveDataRegistry;
    }
    // endregion LiveDataDao

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
