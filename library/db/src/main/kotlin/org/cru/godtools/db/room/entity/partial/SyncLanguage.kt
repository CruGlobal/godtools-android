package org.cru.godtools.db.room.entity.partial

import org.cru.godtools.model.Language

class SyncLanguage(language: Language) {
    val code = language.code
    val name = language.name
    val isForcedName = language.isForcedName
    val apiId = language.apiId
}
