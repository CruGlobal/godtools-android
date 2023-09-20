package org.cru.godtools.db.room.entity.partial

import androidx.room.ColumnInfo
import androidx.room.Ignore
import org.cru.godtools.model.ChangeTrackingModel
import org.cru.godtools.model.Tool

internal class ToolFavorite(val code: String) : ChangeTrackingModel {
    var isFavorite = false
        set(value) {
            if (field != value) markChanged(Tool.ATTR_IS_FAVORITE)
            field = value
        }

    // region ChangeTrackingModel
    @Ignore
    override var isTrackingChanges = false
    @ColumnInfo(name = "changedFields")
    override var changedFieldsStr = ""
    // endregion ChangeTrackingModel
}
