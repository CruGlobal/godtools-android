package org.cru.godtools.xml.model.tips

import androidx.annotation.RestrictTo
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.XMLNS_TRAINING
import org.cru.godtools.xml.model.BaseModel
import org.cru.godtools.xml.model.Manifest
import org.xmlpull.v1.XmlPullParser

private const val XML_TIP = "tip"
private const val XML_TYPE = "type"
private const val XML_TYPE_TIP = "tip"
private const val XML_TYPE_ASK = "ask"
private const val XML_TYPE_CONSIDER = "consider"
private const val XML_TYPE_PREPARE = "prepare"
private const val XML_TYPE_QUOTE = "quote"
private const val XML_PAGES = "pages"

@OptIn(ExperimentalStdlibApi::class)
class Tip : BaseModel {
    enum class Type {
        ASK, CONSIDER, TIP, PREPARE, QUOTE;

        companion object {
            internal val DEFAULT = TIP
        }
    }

    val id: String?
    val type: Type
    val pages: List<TipPage>

    internal constructor(manifest: Manifest, id: String?, parser: XmlPullParser) : super(manifest) {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRAINING, XML_TIP)

        this.id = id
        type = parser.getAttributeValue(null, XML_TYPE)?.toTypeOrNull() ?: Type.DEFAULT
        pages = buildList {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue

                when (parser.namespace) {
                    XMLNS_TRAINING -> when (parser.name) {
                        XML_PAGES -> addAll(parser.parsePages())
                        else -> parser.skipTag()
                    }
                    else -> parser.skipTag()
                }
            }
        }
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(manifest: Manifest? = null, id: String? = null, type: Type = Type.DEFAULT) : super(manifest) {
        this.id = id
        this.type = type
        pages = emptyList()
    }

    private fun XmlPullParser.parsePages() = buildList {
        require(XmlPullParser.START_TAG, XMLNS_TRAINING, XML_PAGES)

        while (next() != XmlPullParser.END_TAG) {
            if (eventType != XmlPullParser.START_TAG) continue

            when (namespace) {
                XMLNS_TRAINING -> when (name) {
                    TipPage.XML_PAGE -> add(TipPage(this@Tip, this@parsePages))
                    else -> skipTag()
                }
                else -> skipTag()
            }
        }
    }
}

private fun String.toTypeOrNull() = when (this) {
    XML_TYPE_TIP -> Tip.Type.TIP
    XML_TYPE_ASK -> Tip.Type.ASK
    XML_TYPE_CONSIDER -> Tip.Type.CONSIDER
    XML_TYPE_PREPARE -> Tip.Type.PREPARE
    XML_TYPE_QUOTE -> Tip.Type.QUOTE
    else -> null
}
