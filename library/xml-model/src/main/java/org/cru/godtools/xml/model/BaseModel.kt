package org.cru.godtools.xml.model

import org.cru.godtools.base.model.Event
import org.xmlpull.v1.XmlPullParser

abstract class BaseModel internal constructor(private val parent: Base? = null) : Base {
    internal companion object {
        internal const val XML_PRIMARY_COLOR = "primary-color"
        internal const val XML_PRIMARY_TEXT_COLOR = "primary-text-color"
        internal const val XML_TEXT_COLOR = "text-color"
        internal const val XML_BACKGROUND_COLOR = "background-color"
        internal const val XML_BACKGROUND_IMAGE = "background-image"
        internal const val XML_BACKGROUND_IMAGE_GRAVITY = "background-image-align"
        internal const val XML_BACKGROUND_IMAGE_SCALE_TYPE = "background-image-scale-type"
        internal const val XML_EVENTS = "events"
        internal const val XML_LISTENERS = "listeners"
        internal const val XML_DISMISS_LISTENERS = "dismiss-listeners"
    }

    override val stylesParent: Styles? get() = parent as? Styles ?: parent?.stylesParent

    override val manifest: Manifest
        get() = parent?.manifest ?: throw IllegalStateException("No manifest found in model ancestors")

    internal open fun getResource(name: String?): Resource? = manifest.getResource(name)

    private val defaultEventNamespace get() = manifest.code
    internal fun parseEvents(parser: XmlPullParser, attribute: String) =
        Event.Id.parse(defaultEventNamespace, parser.getAttributeValue(null, attribute))
}
