package org.cru.godtools.tract.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.IS_CONNECTED_LIVE_DATA
import org.cru.godtools.base.tool.activity.BaseMultiLanguageToolActivityDataModel
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.keynote.godtools.android.db.GodToolsDao

private const val STATE_LIVE_SHARE_TUTORIAL_SHOWN = "liveShareTutorialShown"

@HiltViewModel
class TractActivityDataModel @Inject constructor(
    dao: GodToolsDao,
    downloadManager: GodToolsDownloadManager,
    manifestManager: ManifestManager,
    @Named(IS_CONNECTED_LIVE_DATA) isConnected: LiveData<Boolean>,
    savedState: SavedStateHandle
) : BaseMultiLanguageToolActivityDataModel(dao, downloadManager, manifestManager, isConnected, savedState) {
    var liveShareTutorialShown: Boolean
        get() = savedState[STATE_LIVE_SHARE_TUTORIAL_SHOWN] ?: false
        set(value) {
            savedState[STATE_LIVE_SHARE_TUTORIAL_SHOWN] = value
        }
}
