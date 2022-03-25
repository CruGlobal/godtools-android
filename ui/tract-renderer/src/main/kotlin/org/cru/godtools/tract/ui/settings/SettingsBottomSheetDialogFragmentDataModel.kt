package org.cru.godtools.tract.ui.settings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.model.Language
import org.cru.godtools.model.sortedByDisplayName
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

@HiltViewModel
class SettingsBottomSheetDialogFragmentDataModel @Inject constructor(
    @ApplicationContext context: Context,
    dao: GodToolsDao
) : ViewModel() {
    val toolCode = MutableLiveData<String>()
    val deviceLocale = MutableLiveData(context.deviceLocale)

    private val rawLanguages = toolCode.distinctUntilChanged().switchMap {
        Query.select<Language>()
            .join(LanguageTable.SQL_JOIN_TRANSLATION)
            .where(TranslationTable.FIELD_TOOL.eq(it).and(TranslationTable.SQL_WHERE_PUBLISHED))
            .getAsLiveData(dao)
    }
    val sortedLanguages = deviceLocale.distinctUntilChanged().combineWith(rawLanguages) { locale, languages ->
        languages.sortedByDisplayName(context, locale)
    }
}
