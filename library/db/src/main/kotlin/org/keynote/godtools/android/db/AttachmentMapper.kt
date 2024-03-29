package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.ccci.gto.android.common.db.AbstractMapper
import org.ccci.gto.android.common.util.database.getLong
import org.ccci.gto.android.common.util.database.getString
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Base
import org.keynote.godtools.android.db.Contract.AttachmentTable.COLUMN_DOWNLOADED
import org.keynote.godtools.android.db.Contract.AttachmentTable.COLUMN_FILENAME
import org.keynote.godtools.android.db.Contract.AttachmentTable.COLUMN_LOCALFILENAME
import org.keynote.godtools.android.db.Contract.AttachmentTable.COLUMN_SHA256
import org.keynote.godtools.android.db.Contract.AttachmentTable.COLUMN_TOOL
import org.keynote.godtools.android.db.Contract.BaseTable.Companion.COLUMN_ID

internal object AttachmentMapper : AbstractMapper<Attachment>() {
    override fun mapField(values: ContentValues, field: String, obj: Attachment) {
        when (field) {
            COLUMN_ID -> values.put(field, obj.id)
            COLUMN_TOOL -> values.put(field, obj.toolId)
            COLUMN_FILENAME -> {
                values.put(field, obj.filename)
                values.put(COLUMN_LOCALFILENAME, obj.localFilename)
            }
            COLUMN_SHA256 -> {
                values.put(field, obj.sha256)
                values.put(COLUMN_LOCALFILENAME, obj.localFilename)
            }
            COLUMN_DOWNLOADED -> values.put(field, obj.isDownloaded)
            else -> super.mapField(values, field, obj)
        }
    }

    override fun newObject(c: Cursor) = Attachment()
    override fun toObject(c: Cursor) = super.toObject(c).apply {
        id = c.getLong(COLUMN_ID, Base.INVALID_ID)
        toolId = c.getLong(COLUMN_TOOL)
        filename = c.getString(COLUMN_FILENAME)
        sha256 = c.getString(COLUMN_SHA256)
        isDownloaded = getBool(c, COLUMN_DOWNLOADED, false)
    }
}
