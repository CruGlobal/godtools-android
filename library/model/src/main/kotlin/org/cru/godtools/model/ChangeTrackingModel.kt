package org.cru.godtools.model

interface ChangeTrackingModel : ReadOnlyChangeTrackingModel {
    var isTrackingChanges: Boolean
    override var changedFieldsStr: String

    fun markChanged(field: String) {
        if (isTrackingChanges) changedFieldsStr = "$changedFieldsStr,$field"
    }

    fun clearChanged(field: String) {
        changedFieldsStr = changedFields.filterNot { it == field }.joinToString(",")
    }
}

interface ReadOnlyChangeTrackingModel {
    val changedFields get() = changedFieldsStr.splitToSequence(",").filter { it.isNotEmpty() }.distinct()

    val changedFieldsStr: String
    fun isFieldChanged(field: String) = field in changedFields
}

inline fun <T : ChangeTrackingModel> T.trackChanges(block: (T) -> Unit) {
    isTrackingChanges = true
    try {
        block(this)
    } finally {
        isTrackingChanges = false
    }
}
