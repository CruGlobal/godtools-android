package org.cru.godtools.ui.tooldetails

import com.slack.circuit.runtime.screen.Screen
import java.util.Locale
import kotlinx.parcelize.Parcelize

@Parcelize
class ToolDetailsScreen(val initialTool: String, val secondLanguage: Locale? = null) : Screen
