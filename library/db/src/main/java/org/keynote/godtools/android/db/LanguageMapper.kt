package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.ccci.gto.android.common.util.database.getString
import org.cru.godtools.model.Language
import org.keynote.godtools.android.db.Contract.LanguageTable

internal object LanguageMapper : BaseMapper<Language?>() {
    override fun mapField(values: ContentValues, field: String, language: Language) {
        when (field) {
            LanguageTable.COLUMN_CODE -> values.put(field, serialize(language.code))
            LanguageTable.COLUMN_ADDED -> values.put(field, language.isAdded)
            LanguageTable.COLUMN_NAME -> values.put(field, language.name)
            else -> super.mapField(values, field, language)
        }
    }

    override fun newObject(c: Cursor) = Language()
    override fun toObject(c: Cursor) = super.toObject(c).apply {
        setCode(getLocale(c, LanguageTable.COLUMN_CODE, Language.INVALID_CODE))
        isAdded = getBool(c, LanguageTable.COLUMN_ADDED, false)
        name = c.getString(LanguageTable.COLUMN_NAME)
    }
}
