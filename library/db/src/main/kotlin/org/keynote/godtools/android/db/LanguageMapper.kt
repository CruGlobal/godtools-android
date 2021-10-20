package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.ccci.gto.android.common.util.database.getLocale
import org.ccci.gto.android.common.util.database.getString
import org.cru.godtools.model.Language
import org.keynote.godtools.android.db.Contract.LanguageTable.COLUMN_ADDED
import org.keynote.godtools.android.db.Contract.LanguageTable.COLUMN_CODE
import org.keynote.godtools.android.db.Contract.LanguageTable.COLUMN_NAME

internal object LanguageMapper : BaseMapper<Language>() {
    override fun mapField(values: ContentValues, field: String, obj: Language) {
        when (field) {
            COLUMN_CODE -> values.put(field, serialize(obj.code))
            COLUMN_NAME -> values.put(field, obj.name)
            COLUMN_ADDED -> values.put(field, obj.isAdded)
            else -> super.mapField(values, field, obj)
        }
    }

    override fun newObject(c: Cursor) = Language()
    override fun toObject(c: Cursor) = super.toObject(c).apply {
        code = c.getLocale(COLUMN_CODE, Language.INVALID_CODE)
        name = c.getString(COLUMN_NAME)
        isAdded = getBool(c, COLUMN_ADDED, false)
    }
}
