package org.cru.godtools.xml.util

import org.cru.godtools.xml.service.xmlPullParser
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

fun Any.getXmlParserForResource(name: String): XmlPullParser = getInputStreamForResource(name).xmlPullParser()

fun Any.getInputStreamForResource(name: String): InputStream = this::class.java.getResourceAsStream(name)!!
