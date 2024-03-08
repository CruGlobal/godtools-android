package org.cru.godtools.ui.tooldetails

import com.slack.circuit.runtime.screen.Screen
import java.util.Locale
import kotlinx.parcelize.Parcelize
import org.cru.godtools.model.Tool

/**
 * This is a temporary screen to help bridge functionality until Tutorials use Circuit.
 */
@Parcelize
internal class OpenToolTrainingScreen(val tool: String?, val type: Tool.Type, val locale: Locale?) : Screen
