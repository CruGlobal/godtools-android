package org.cru.godtools.xml.util

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

fun Any.getXmlParserForResource(name: String): XmlPullParser = Xml.newPullParser().apply {
    setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
    setInput(this@getXmlParserForResource.getInputStreamForResource(name), "UTF-8")
    nextTag()
}

fun Any.getInputStreamForResource(name: String): InputStream = this::class.java.getResourceAsStream(name)!!
