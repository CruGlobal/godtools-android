package org.keynote.godtools.android.db;

import android.content.ContentValues;
import android.database.Cursor;

import org.cru.godtools.model.Attachment;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Tool.Type;

import androidx.annotation.NonNull;

import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_ADDED;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_BANNER;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_CODE;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_DESCRIPTION;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_DETAILS_BANNER;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_NAME;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_ORDER;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_OVERVIEW_VIDEO;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_PENDING_SHARES;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_SHARES;
import static org.keynote.godtools.android.db.Contract.ToolTable.COLUMN_TYPE;

final class ToolMapper extends BaseMapper<Tool> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Tool tool) {
        switch (field) {
            case COLUMN_CODE:
                values.put(field, tool.getCode());
                break;
            case COLUMN_TYPE:
                values.put(field, serialize(tool.getType()));
                break;
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
            case COLUMN_OVERVIEW_VIDEO:
                values.put(field, tool.getOverviewVideo());
                break;
            case COLUMN_ORDER:
                values.put(field, tool.getOrder());
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

        tool.setCode(getString(c, COLUMN_CODE));
        tool.setType(getEnum(c, COLUMN_TYPE, Type.class, null));
        tool.setName(getString(c, COLUMN_NAME, null));
        tool.setDescription(getString(c, COLUMN_DESCRIPTION, null));
        tool.setShares(getInt(c, COLUMN_SHARES, 0));
        tool.setPendingShares(getInt(c, COLUMN_PENDING_SHARES, 0));
        tool.setBannerId(getLong(c, COLUMN_BANNER, Attachment.INVALID_ID));
        tool.setDetailsBannerId(getLong(c, COLUMN_DETAILS_BANNER, Attachment.INVALID_ID));
        tool.setOverviewVideo(getString(c, COLUMN_OVERVIEW_VIDEO, null));
        tool.setOrder(getInt(c, COLUMN_ORDER, Integer.MAX_VALUE));
        tool.setAdded(getBool(c, COLUMN_ADDED, false));

        return tool;
    }
}
