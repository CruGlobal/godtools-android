package org.cru.godtools.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiId
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore

abstract class Base {
    companion object {
        const val INVALID_ID: Long = -1
    }

    @JsonApiId
    private var _id: Long? = INVALID_ID
    var id: Long
        get() = _id ?: INVALID_ID
        set(id) {
            _id = id
        }

    @JsonApiIgnore
    private var stashedId: Long? = null

    fun stashId() {
        stashedId = _id
        _id = null
    }
}
