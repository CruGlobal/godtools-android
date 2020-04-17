package org.cru.godtools.model.event

object AttachmentUpdateEvent
object LanguageUpdateEvent
object ToolUpdateEvent
object TranslationUpdateEvent

/**
 * This event is fired when a tool is actually opened.
 */
class ToolUsedEvent(val toolCode: String)
