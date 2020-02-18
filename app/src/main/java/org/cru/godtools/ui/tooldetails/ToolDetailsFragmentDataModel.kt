package org.cru.godtools.ui.tooldetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import org.ccci.gto.android.common.db.findLiveData
import org.ccci.gto.android.common.lifecycle.orEmpty
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.GodToolsDao

class ToolDetailsFragmentDataModel(application: Application) : AndroidViewModel(application) {
    private val dao = GodToolsDao.getInstance(application)

    val toolCode = MutableLiveData<String>()

    val tool = toolCode.distinctUntilChanged().switchMap { it?.let { dao.findLiveData<Tool>(it) }.orEmpty() }
}
