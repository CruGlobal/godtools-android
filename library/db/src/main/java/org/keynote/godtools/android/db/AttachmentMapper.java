package org.keynote.godtools.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.cru.godtools.model.Attachment;
import org.cru.godtools.model.Tool;

import static org.keynote.godtools.android.db.Contract.AttachmentTable.COLUMN_DOWNLOADED;
import static org.keynote.godtools.android.db.Contract.AttachmentTable.COLUMN_FILENAME;
import static org.keynote.godtools.android.db.Contract.AttachmentTable.COLUMN_LOCALFILENAME;
import static org.keynote.godtools.android.db.Contract.AttachmentTable.COLUMN_SHA256;
import static org.keynote.godtools.android.db.Contract.AttachmentTable.COLUMN_TOOL;

final class AttachmentMapper extends BaseMapper<Attachment> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Attachment attachment) {
        switch (field) {
            case COLUMN_TOOL:
                values.put(field, attachment.getToolId());
                break;
            case COLUMN_FILENAME:
                values.put(field, attachment.getFileName());
                values.put(COLUMN_LOCALFILENAME, attachment.getLocalFileName());
                break;
            case COLUMN_SHA256:
                values.put(field, attachment.getSha256());
                values.put(COLUMN_LOCALFILENAME, attachment.getLocalFileName());
                break;
            case COLUMN_DOWNLOADED:
                values.put(field, attachment.isDownloaded());
                break;
            default:
                super.mapField(values, field, attachment);
                break;
        }
    }

    @NonNull
    @Override
    protected Attachment newObject(@NonNull final Cursor c) {
        return new Attachment();
    }

    @NonNull
    @Override
    public Attachment toObject(@NonNull final Cursor c) {
        final Attachment attachment = super.toObject(c);

        attachment.setToolId(getLong(c, COLUMN_TOOL, Tool.INVALID_ID));
        attachment.setFileName(getString(c, COLUMN_FILENAME, null));
        attachment.setSha256(getString(c, COLUMN_SHA256, null));
        attachment.setDownloaded(getBool(c, COLUMN_DOWNLOADED, false));

        return attachment;
    }
}
