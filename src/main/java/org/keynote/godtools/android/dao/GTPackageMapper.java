package org.keynote.godtools.android.dao;

import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.COL_CODE;
import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.COL_CONFIG_FILE_NAME;
import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.COL_ICON;
import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.COL_LANGUAGE;
import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.COL_NAME;
import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.COL_STATUS;
import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.COL_VERSION;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.keynote.godtools.android.business.GTPackage;

public class GTPackageMapper extends AbstractMapper<GTPackage> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final GTPackage obj) {
        switch (field) {
            case COL_CODE:
                values.put(field, obj.getCode());
                break;
            case COL_NAME:
                values.put(field, obj.getName());
                break;
            case COL_LANGUAGE:
                values.put(field, obj.getLanguage());
                break;
            case COL_VERSION:
                values.put(field, obj.getVersion());
                break;
            case COL_CONFIG_FILE_NAME:
                values.put(field, obj.getConfigFileName());
                break;
            case COL_STATUS:
                values.put(field, obj.getStatus());
                break;
            case COL_ICON:
                values.put(field, obj.getIcon());
                break;
            default:
                super.mapField(values, field, obj);
                break;
        }
    }

    @NonNull
    @Override
    protected GTPackage newObject(@NonNull final Cursor c) {
        return new GTPackage();
    }

    @NonNull
    @Override
    public GTPackage toObject(@NonNull final Cursor c) {
        final GTPackage gtPackage = super.toObject(c);

        gtPackage.setCode(getString(c, COL_CODE));
        gtPackage.setName(getString(c, COL_NAME));
        gtPackage.setLanguage(getString(c, COL_LANGUAGE));
        gtPackage.setVersion(getDouble(c, COL_VERSION));
        gtPackage.setConfigFileName(getString(c, COL_CONFIG_FILE_NAME));
        gtPackage.setStatus(getString(c, COL_STATUS));
        gtPackage.setIcon(getString(c, COL_ICON));

        return gtPackage;
    }
}
