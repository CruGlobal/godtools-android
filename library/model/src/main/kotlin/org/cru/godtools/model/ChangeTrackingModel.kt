package org.cru.godtools.model

interface ChangeTrackingModel {
    val changedFields get() = changedFieldsStr.splitToSequence(",").filter { it.isNotEmpty() }.distinct()

    var isTrackingChanges: Boolean
    var changedFieldsStr: String

    fun markChanged(field: String) {
        if (isTrackingChanges) changedFieldsStr = "$changedFieldsStr,$field"
    }
}
