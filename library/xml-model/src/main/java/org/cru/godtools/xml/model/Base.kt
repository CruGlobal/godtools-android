package org.cru.godtools.xml.model

import android.view.View

val BaseModel?.layoutDirection get() = this?.layoutDirection ?: View.LAYOUT_DIRECTION_INHERIT
