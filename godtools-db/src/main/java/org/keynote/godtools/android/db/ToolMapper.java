package org.keynote.godtools.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.cru.godtools.model.Attachment;
import org.keynote.godtools.android.model.Tool;

import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_ADDED;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_BANNER;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_COPYRIGHT;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_DESCRIPTION;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_DETAILS_BANNER;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_NAME;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_PENDING_SHARES;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_SHARES;

final class ToolMapper extends BaseMapper<Tool> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Tool tool) {
        switch (field) {
            case COLUMN_NAME:
                values.put(field, tool.getName());
                break;
            case COLUMN_DESCRIPTION:
                values.put(field, tool.getDescription());
                break;
            case COLUMN_SHARES:
                values.put(field, tool.getShares());
                break;
            case COLUMN_PENDING_SHARES:
                values.put(field, tool.getPendingShares());
                break;
            case COLUMN_BANNER:
                values.put(field, tool.getBannerId());
                break;
            case COLUMN_DETAILS_BANNER:
                values.put(field, tool.getDetailsBannerId());
                break;
            case COLUMN_COPYRIGHT:
                values.put(field, tool.getCopyright());
                break;
            case COLUMN_ADDED:
                values.put(field, tool.isAdded());
                break;
            default:
                super.mapField(values, field, tool);
                break;
        }
    }

    @NonNull
    @Override
    protected Tool newObject(@NonNull final Cursor c) {
        return new Tool();
    }

    @NonNull
    @Override
    public Tool toObject(@NonNull final Cursor c) {
        final Tool tool = super.toObject(c);

        tool.setName(getString(c, COLUMN_NAME, null));
        tool.setDescription(getString(c, COLUMN_DESCRIPTION, null));
        tool.setShares(getInt(c, COLUMN_SHARES, 0));
        tool.setPendingShares(getInt(c, COLUMN_PENDING_SHARES, 0));
        tool.setBannerId(getLong(c, COLUMN_BANNER, Attachment.INVALID_ID));
        tool.setDetailsBannerId(getLong(c, COLUMN_DETAILS_BANNER, Attachment.INVALID_ID));
        tool.setCopyright(getString(c, COLUMN_COPYRIGHT, null));
        tool.setAdded(getBool(c, COLUMN_ADDED, false));

        return tool;
    }
}
