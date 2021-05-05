package org.cru.godtools.xml.model

import org.xmlpull.v1.XmlPullParser

abstract class BaseModel internal constructor(private val parent: Base? = null) : Base {
    override val stylesParent: Styles? get() = parent as? Styles ?: parent?.stylesParent

    override val manifest: Manifest
        get() = parent?.manifest ?: throw IllegalStateException("No manifest found in model ancestors")

    internal open fun getResource(name: String?): Resource? = manifest.getResource(name)

    private val defaultEventNamespace get() = manifest.code
    internal fun parseEvents(parser: XmlPullParser, attribute: String) =
        EventId.parse(defaultEventNamespace, parser.getAttributeValue(null, attribute))
}
