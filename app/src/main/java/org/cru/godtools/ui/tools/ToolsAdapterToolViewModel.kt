package org.cru.godtools.ui.tools

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import org.ccci.gto.android.common.androidx.lifecycle.orEmpty
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.model.Attachment
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao
import javax.inject.Inject

class ToolsAdapterToolViewModel @Inject constructor(private val dao: GodToolsDao) : ViewModel() {
    val toolCode = MutableLiveData<String?>()
    private val distinctToolCode = toolCode.distinctUntilChanged()

    val banner = distinctToolCode.switchMap {
        it?.let {
            Query.select<Attachment>()
                .join(AttachmentTable.SQL_JOIN_TOOL)
                .where(
                    ToolTable.FIELD_CODE.eq(it)
                        .and(ToolTable.FIELD_BANNER.eq(AttachmentTable.FIELD_ID))
                        .and(AttachmentTable.SQL_WHERE_DOWNLOADED)
                )
                .limit(1)
                .getAsLiveData(dao)
        }.orEmpty()
    }.map { it?.firstOrNull() }
}
