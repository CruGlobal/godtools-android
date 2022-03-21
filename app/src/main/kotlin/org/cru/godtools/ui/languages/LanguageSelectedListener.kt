package org.cru.godtools.ui.languages

import org.cru.godtools.model.Language

interface LanguageSelectedListener {
    fun onLanguageSelected(language: Language?)
}
