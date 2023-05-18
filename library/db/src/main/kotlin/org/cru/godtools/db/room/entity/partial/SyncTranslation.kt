package org.cru.godtools.db.room.entity.partial

import org.cru.godtools.model.Translation

class SyncTranslation(translation: Translation) {
    val id = translation.id
    val tool = translation.toolCode
    val locale = translation.languageCode
    val version = translation.version
    val name = translation.name
    val description = translation.description
    val tagline = translation.tagline
    val toolDetailsConversationStarters = translation.toolDetailsConversationStarters
    val toolDetailsOutline = translation.toolDetailsOutline
    val toolDetailsBibleReferences = translation.toolDetailsBibleReferences
    val manifestFileName = translation.manifestFileName
}
