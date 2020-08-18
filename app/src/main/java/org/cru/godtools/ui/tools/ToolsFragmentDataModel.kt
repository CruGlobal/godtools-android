package org.cru.godtools.ui.tools

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_TOOL_FAVORITE
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_TRAINING
import org.cru.godtools.model.Tool
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.ui.tools.ToolsFragment.Companion.MODE_ADDED
import org.cru.godtools.ui.tools.ToolsFragment.Companion.MODE_ALL
import org.cru.godtools.ui.tools.ToolsFragment.Companion.MODE_AVAILABLE
import org.cru.godtools.widget.BannerType
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

class ToolsFragmentDataModel @Inject constructor(private val dao: GodToolsDao, settings: Settings) : ViewModel() {
    val mode = MutableLiveData(MODE_ADDED)

    val tools = mode.distinctUntilChanged().switchMap { mode ->
        Query.select<Tool>()
            .where(ToolTable.FIELD_TYPE.ne(Tool.Type.UNKNOWN).let {
                when (mode) {
                    MODE_ADDED, MODE_AVAILABLE -> it.and(ToolTable.FIELD_ADDED.eq(mode == MODE_ADDED))
                    else -> it
                }
            })
            .orderBy(
                when (mode) {
                    MODE_ADDED -> ToolTable.SQL_ORDER_BY_ORDER
                    else -> ToolTable.COLUMN_DEFAULT_ORDER
                }
            )
            .getAsLiveData(dao)
    }

    val banner = mode.combineWith(
        settings.isFeatureDiscoveredLiveData(FEATURE_TOOL_FAVORITE),
        settings.isFeatureDiscoveredLiveData(FEATURE_TUTORIAL_TRAINING)
    ) { mode, favorite, training ->
        when {
            mode == MODE_ADDED && !training && PageSet.TRAINING.supportsLocale(Locale.getDefault()) ->
                BannerType.TUTORIAL_TRAINING
            mode == MODE_ALL && !favorite -> BannerType.TOOL_LIST_FAVORITES
            else -> null
        }
    }
}
