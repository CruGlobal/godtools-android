package org.cru.godtools.tract.databinding

import android.text.InputType
import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.cru.godtools.tool.model.Input

@BindingAdapter("android:inputType")
internal fun TextView.bindInputType(type: Input.Type?) {
    inputType = when (type) {
        Input.Type.EMAIL -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        Input.Type.PHONE -> InputType.TYPE_CLASS_PHONE
        else -> InputType.TYPE_CLASS_TEXT
    }
}
