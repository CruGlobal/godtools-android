package org.cru.godtools.xml.model

import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import org.cru.godtools.base.model.Event
import org.xmlpull.v1.XmlPullParser

abstract class Base : BaseModel {
    companion object {
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

    internal constructor() {
        parent = this
    }

    internal constructor(parent: Base) {
        this.parent = parent
    }

    private val parent: BaseModel

    override val stylesParent: Styles?
        get() = when {
            parent === this -> null
            else -> (parent as? Styles) ?: parent.stylesParent
        }

    override val manifest: Manifest
        get() {
            check(parent !== this) { "No manifest found in model ancestors" }
            return parent.manifest
        }
    override val page: Page
        get() {
            check(parent !== this) { "No page found in model ancestors" }
            return parent.page
        }

    open fun getResource(name: String?): Resource? = manifest.getResource(name)

    internal val layoutDirection get() = TextUtilsCompat.getLayoutDirectionFromLocale(manifest.locale)

    private val defaultEventNamespace get() = manifest.code
    fun parseEvents(parser: XmlPullParser, attribute: String) =
        Event.Id.parse(defaultEventNamespace, parser.getAttributeValue(null, attribute))
}

val Base?.layoutDirection get() = this?.layoutDirection ?: ViewCompat.LAYOUT_DIRECTION_INHERIT
