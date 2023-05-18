package org.keynote.godtools.android.db

import android.content.ContentValues
import android.database.Cursor
import org.ccci.gto.android.common.util.database.getInt
import org.ccci.gto.android.common.util.database.getLocale
import org.ccci.gto.android.common.util.database.getString
import org.cru.godtools.model.Language
import org.cru.godtools.model.Translation
import org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_DESCRIPTION
import org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_DETAILS_BIBLE_REFERENCES
import org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_DETAILS_CONVERSATION_STARTERS
import org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_DETAILS_OUTLINE
import org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_DOWNLOADED
import org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_LANGUAGE
import org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_MANIFEST
import org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_NAME
import org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_PUBLISHED
import org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_TAGLINE
import org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_TOOL
import org.keynote.godtools.android.db.Contract.TranslationTable.COLUMN_VERSION

internal object TranslationMapper : BaseMapper<Translation>() {
    override fun mapField(values: ContentValues, field: String, obj: Translation) {
        when (field) {
            COLUMN_TOOL -> values.put(field, obj.toolCode)
            COLUMN_LANGUAGE -> values.put(field, serialize(obj.languageCode))
            COLUMN_VERSION -> values.put(field, obj.version)
            COLUMN_NAME -> values.put(field, obj.name)
            COLUMN_DESCRIPTION -> values.put(field, obj.description)
            COLUMN_TAGLINE -> values.put(field, obj.tagline)
            COLUMN_DETAILS_OUTLINE -> values.put(field, obj.toolDetailsOutline)
            COLUMN_DETAILS_BIBLE_REFERENCES -> values.put(field, obj.toolDetailsBibleReferences)
            COLUMN_DETAILS_CONVERSATION_STARTERS -> values.put(field, obj.toolDetailsConversationStarters)
            COLUMN_MANIFEST -> values.put(field, obj.manifestFileName)
            COLUMN_PUBLISHED -> values.put(field, true)
            COLUMN_DOWNLOADED -> values.put(field, obj.isDownloaded)
            else -> super.mapField(values, field, obj)
        }
    }

    override fun newObject(c: Cursor) = Translation()
    override fun toObject(c: Cursor) = super.toObject(c).apply {
        toolCode = c.getString(COLUMN_TOOL)
        languageCode = c.getLocale(COLUMN_LANGUAGE, Language.INVALID_CODE)
        version = c.getInt(COLUMN_VERSION, Translation.DEFAULT_VERSION)
        name = c.getString(COLUMN_NAME)
        description = c.getString(COLUMN_DESCRIPTION)
        tagline = c.getString(COLUMN_TAGLINE)
        toolDetailsOutline = c.getString(COLUMN_DETAILS_OUTLINE)
        toolDetailsBibleReferences = c.getString(COLUMN_DETAILS_BIBLE_REFERENCES)
        toolDetailsConversationStarters = c.getString(COLUMN_DETAILS_CONVERSATION_STARTERS)
        manifestFileName = c.getString(COLUMN_MANIFEST)
        isDownloaded = getBool(c, COLUMN_DOWNLOADED, false)
    }
}
