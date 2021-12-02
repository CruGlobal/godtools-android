package org.cru.godtools.ui.tools

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.db.Expression.Companion.constants
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.BuildConfig
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_TOOL_FAVORITE
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_TRAINING
import org.cru.godtools.model.Tool
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.ui.tools.ToolsFragment.Companion.MODE_ADDED
import org.cru.godtools.ui.tools.ToolsFragment.Companion.MODE_ALL
import org.cru.godtools.ui.tools.ToolsFragment.Companion.MODE_LESSONS
import org.cru.godtools.widget.BannerType
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

@HiltViewModel
class ToolsFragmentDataModel @Inject constructor(private val dao: GodToolsDao, settings: Settings) :
    ViewModel() {
    val mode = MutableLiveData(MODE_ADDED)

    val tools = mode.distinctUntilChanged().switchMap { mode ->
        var where = ToolTable.FIELD_TYPE.`in`(
            *when (mode) {
                MODE_LESSONS -> constants(Tool.Type.LESSON)
                else -> constants(Tool.Type.TRACT, Tool.Type.ARTICLE, Tool.Type.CYOA)
            }
        ).and(ToolTable.FIELD_HIDDEN.ne(true))
        if (mode == MODE_ADDED) where = where.and(ToolTable.FIELD_ADDED.eq(true))
        // HACK: disable CYOA tools for release builds
        if (!BuildConfig.DEBUG) where = where.and(ToolTable.FIELD_TYPE.ne(Tool.Type.CYOA))
        Query.select<Tool>()
            .where(where)
            .orderBy(if (mode == MODE_ADDED) ToolTable.SQL_ORDER_BY_ORDER else ToolTable.COLUMN_DEFAULT_ORDER)
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
