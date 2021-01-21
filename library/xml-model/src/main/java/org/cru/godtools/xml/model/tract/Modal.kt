package org.cru.godtools.xml.model.tract

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.RestrictTo
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.R
import org.cru.godtools.xml.XMLNS_TRACT
import org.cru.godtools.xml.model.BaseModel
import org.cru.godtools.xml.model.Content
import org.cru.godtools.xml.model.Parent
import org.cru.godtools.xml.model.Styles
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.model.parseContent
import org.xmlpull.v1.XmlPullParser

private const val XML_TITLE = "title"

class Modal : BaseModel, Parent, Styles {
    companion object {
        internal const val XML_MODAL = "modal"
    }

    val page: TractPage
    val id get() = "${page.id}-$position"
    private val position: Int

    val title: Text?
    override val content: List<Content>

    val listeners: Set<Event.Id>
    val dismissListeners: Set<Event.Id>

    @get:ColorInt
    override val primaryColor get() = Color.TRANSPARENT
    @get:ColorInt
    override val primaryTextColor get() = Color.WHITE
    @get:ColorInt
    override val textColor get() = Color.WHITE
    @get:ColorInt
    override val buttonColor get() = Color.WHITE

    @get:DimenRes
    override val textSize get() = R.dimen.text_size_modal
    override val textAlign get() = Text.Align.CENTER

    @RestrictTo(RestrictTo.Scope.TESTS)
    internal constructor(parent: TractPage, position: Int) : super(parent) {
        page = parent
        this.position = position
        title = null
        content = emptyList()
        listeners = emptySet()
        dismissListeners = emptySet()
    }

    internal constructor(parent: TractPage, position: Int, parser: XmlPullParser) : super(parent) {
        page = parent
        this.position = position

        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_MODAL)

        listeners = parseEvents(parser, XML_LISTENERS)
        dismissListeners = parseEvents(parser, XML_DISMISS_LISTENERS)

        // process any child elements
        var title: Text? = null
        content = parseContent(parser) {
            when (parser.namespace) {
                XMLNS_TRACT -> when (parser.name) {
                    XML_TITLE -> title = Text.fromNestedXml(this@Modal, parser, XMLNS_TRACT, XML_TITLE)
                }
            }
        }
        this.title = title
    }
}
