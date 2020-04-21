package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.ccci.gto.android.common.db.AbstractMapper
import org.ccci.gto.android.common.util.database.getLong
import org.ccci.gto.android.common.util.database.getString
import org.cru.godtools.model.Base
import org.cru.godtools.model.LocalFile
import org.cru.godtools.model.TranslationFile
import org.keynote.godtools.android.db.Contract.TranslationFileTable.COLUMN_FILE
import org.keynote.godtools.android.db.Contract.TranslationFileTable.COLUMN_TRANSLATION

internal object TranslationFileMapper : AbstractMapper<TranslationFile>() {
    override fun mapField(values: ContentValues, field: String, file: TranslationFile) {
        when (field) {
            COLUMN_TRANSLATION -> values.put(field, file.translationId)
            COLUMN_FILE -> values.put(field, file.fileName)
            else -> super.mapField(values, field, file)
        }
    }

    override fun newObject(c: Cursor) = TranslationFile()
    override fun toObject(c: Cursor) = super.toObject(c).apply {
        setTranslationId(c.getLong(COLUMN_TRANSLATION, Base.INVALID_ID))
        fileName = c.getString(COLUMN_FILE, LocalFile.INVALID_FILE_NAME)
    }
}
