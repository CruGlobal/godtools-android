package org.cru.godtools.ui.tooldetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.util.Locale
import org.cru.godtools.model.Tool

private const val KEY_TOOL = "KEY_TIPS_TOOL"
private const val KEY_TYPE = "KEY_TIPS_TYPE"
private const val KEY_LANGUAGE = "KEY_TIPS_LANGUAGE"

class SelectedToolSavedState(private val savedState: SavedStateHandle) : ViewModel() {
    var tool
        get() = savedState.get<String>(KEY_TOOL)
        set(value) = savedState.set(KEY_TOOL, value)
    var type
        get() = savedState.get<Tool.Type>(KEY_TYPE)
        set(value) = savedState.set(KEY_TYPE, value)
    var language
        get() = savedState.get<Locale>(KEY_LANGUAGE)
        set(value) = savedState.set(KEY_LANGUAGE, value)
}
