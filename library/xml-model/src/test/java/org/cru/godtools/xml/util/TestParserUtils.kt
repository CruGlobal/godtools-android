package org.cru.godtools.xml.util

import android.util.Xml
import org.xmlpull.v1.XmlPullParser

fun Any.getXmlParserForResource(name: String): XmlPullParser = Xml.newPullParser().apply {
    setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
    setInput(this@getXmlParserForResource::class.java.getResourceAsStream(name), "UTF-8")
    nextTag()
}
