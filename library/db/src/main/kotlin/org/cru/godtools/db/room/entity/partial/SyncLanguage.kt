package org.cru.godtools.db.room.entity.partial

import org.cru.godtools.model.Language

class SyncLanguage(language: Language) {
    val id = language.id
    val code = language.code
    val name = language.name
}
