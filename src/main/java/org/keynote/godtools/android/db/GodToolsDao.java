package org.keynote.godtools.android.db;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.db.Expression;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.db.Contract.FollowupTable;
import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.db.Contract.ResourceTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.model.Base;
import org.keynote.godtools.android.model.Followup;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.model.Resource;
import org.keynote.godtools.android.model.Translation;

public class GodToolsDao extends DBAdapter {
    private GodToolsDao(@NonNull final Context context) {
        super(GodToolsDatabase.getInstance(context));

        registerType(Followup.class, FollowupTable.TABLE_NAME, FollowupTable.PROJECTION_ALL, new FollowupMapper(),
                     FollowupTable.SQL_WHERE_PRIMARY_KEY);
        registerType(Language.class, LanguageTable.TABLE_NAME, LanguageTable.PROJECTION_ALL, new LanguageMapper(),
                     LanguageTable.SQL_WHERE_PRIMARY_KEY);
        registerType(Resource.class, ResourceTable.TABLE_NAME, ResourceTable.PROJECTION_ALL, new ResourceMapper(),
                     ResourceTable.SQL_WHERE_PRIMARY_KEY);
        registerType(Translation.class, TranslationTable.TABLE_NAME, TranslationTable.PROJECTION_ALL,
                     new TranslationMapper(), TranslationTable.SQL_WHERE_PRIMARY_KEY);
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
    protected Expression getPrimaryKeyWhere(@NonNull final Object obj) {
        if (obj instanceof Followup) {
            final Followup followup = (Followup) obj;
            return getPrimaryKeyWhere(Followup.class, followup.getId(), followup.getContextId());
        } else if (obj instanceof Base) {
            return getPrimaryKeyWhere(obj.getClass(), ((Base) obj).getId());
        }

        return super.getPrimaryKeyWhere(obj);
    }
}
