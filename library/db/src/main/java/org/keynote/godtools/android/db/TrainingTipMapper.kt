package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.ccci.gto.android.common.db.AbstractMapper
import org.ccci.gto.android.common.util.database.getLocale
import org.ccci.gto.android.common.util.database.getString
import org.cru.godtools.model.Language
import org.cru.godtools.model.TrainingTip
import org.keynote.godtools.android.db.Contract.TrainingTipTable.COLUMN_IS_COMPLETED
import org.keynote.godtools.android.db.Contract.TrainingTipTable.COLUMN_LANGUAGE
import org.keynote.godtools.android.db.Contract.TrainingTipTable.COLUMN_TIP_ID
import org.keynote.godtools.android.db.Contract.TrainingTipTable.COLUMN_TOOL

internal object TrainingTipMapper : AbstractMapper<TrainingTip>() {
    override fun mapField(values: ContentValues, field: String, obj: TrainingTip) {
        when (field) {
            COLUMN_TOOL -> values.put(field, obj.tool)
            COLUMN_LANGUAGE -> values.put(field, serialize(obj.locale))
            COLUMN_TIP_ID -> values.put(field, obj.tipId)
            COLUMN_IS_COMPLETED -> values.put(field, obj.isCompleted)
            else -> super.mapField(values, field, obj)
        }
    }

    override fun newObject(c: Cursor) = TrainingTip(
        c.getString(COLUMN_TOOL).orEmpty(),
        c.getLocale(COLUMN_LANGUAGE) ?: Language.INVALID_CODE,
        c.getString(COLUMN_TIP_ID).orEmpty()
    )
    override fun toObject(c: Cursor) = super.toObject(c).apply {
        isCompleted = getBool(c, COLUMN_IS_COMPLETED, false)
    }
}
