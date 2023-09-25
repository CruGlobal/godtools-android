package org.cru.godtools.model

interface ChangeTrackingModel {
    val changedFields get() = changedFieldsStr.splitToSequence(",").filter { it.isNotEmpty() }.distinct()

    var isTrackingChanges: Boolean
    var changedFieldsStr: String
    fun isFieldChanged(field: String) = field in changedFields

    fun markChanged(field: String) {
        if (isTrackingChanges) changedFieldsStr = "$changedFieldsStr,$field"
    }

    fun clearChanged(field: String) {
        changedFieldsStr = changedFields.filterNot { it == field }.joinToString(",")
    }
}

inline fun <T : ChangeTrackingModel> T.trackChanges(block: (T) -> Unit) {
    isTrackingChanges = true
    try {
        block(this)
    } finally {
        isTrackingChanges = false
    }
}
