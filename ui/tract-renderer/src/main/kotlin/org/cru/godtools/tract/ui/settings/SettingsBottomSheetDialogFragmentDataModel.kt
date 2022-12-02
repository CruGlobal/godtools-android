package org.cru.godtools.tract.ui.settings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Translation
import org.cru.godtools.model.sortedByDisplayName
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsBottomSheetDialogFragmentDataModel @Inject constructor(
    @ApplicationContext context: Context,
    dao: GodToolsDao,
    languagesRepository: LanguagesRepository,
) : ViewModel() {
    val toolCode = MutableStateFlow<String?>(null)
    val deviceLocale = MutableLiveData(context.deviceLocale)

    private val rawLanguages = toolCode
        .flatMapLatest {
            it?.let {
                Query.select<Translation>()
                    .distinct(true)
                    .projection(TranslationTable.COLUMN_LANGUAGE)
                    .where(TranslationTable.SQL_WHERE_PUBLISHED.and(TranslationTable.FIELD_TOOL.eq(it)))
                    .getAsFlow(dao)
            } ?: flowOf(emptyList())
        }
        .flatMapLatest { languagesRepository.getLanguagesForLocalesFlow(it.map { it.languageCode }) }
        .asLiveData()
    val sortedLanguages = deviceLocale.distinctUntilChanged()
        .combineWith(rawLanguages) { locale, languages -> languages.sortedByDisplayName(context, locale) }
        .distinctUntilChanged()
}
