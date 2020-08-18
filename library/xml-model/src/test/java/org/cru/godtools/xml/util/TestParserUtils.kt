package org.cru.godtools.xml.util

import java.io.InputStream
import org.cru.godtools.xml.service.xmlPullParser

fun Any.getXmlParserForResource(name: String) = getInputStreamForResource(name).xmlPullParser()

fun Any.getInputStreamForResource(name: String): InputStream = this::class.java.getResourceAsStream(name)!!
