package org.cru.godtools.xml.service

import androidx.annotation.AnyThread
import androidx.annotation.VisibleForTesting
import java.io.InputStream
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.kotlin.coroutines.MutexMap
import org.ccci.gto.android.common.kotlin.coroutines.withLock
import org.ccci.gto.android.common.support.v4.util.WeakLruCache
import org.ccci.gto.android.common.util.xmlpull.CloseableXmlPullParser
import org.cru.godtools.base.ToolFileManager
import org.cru.godtools.tool.service.ManifestParser
import org.cru.godtools.tool.service.Result
import org.cru.godtools.tool.xml.AndroidXmlPullParserFactory
import org.xmlpull.v1.XmlPullParser

@Singleton
class ManifestParser @Inject internal constructor(private val fileManager: ToolFileManager) {
    private val cache = WeakLruCache<String, Result.Data>(6)
    private val loadingMutex = MutexMap()

    // TODO: make this a concrete class and not just an anonymous object
    private val parser = ManifestParser(object : AndroidXmlPullParserFactory() {
        override fun openFile(fileName: String) = runBlocking { fileManager.getInputStream(fileName).buffered() }
    })

    @AnyThread
    suspend fun parse(manifestName: String, toolCode: String, locale: Locale) = loadingMutex.withLock(manifestName) {
        cache[manifestName] ?: withContext(Dispatchers.IO) {
            parser.parseManifest(manifestName)
                .also { if (it is Result.Data) cache.put(manifestName, it) }
        }
    }
}

@VisibleForTesting
internal fun InputStream.xmlPullParser() = CloseableXmlPullParser().also {
    it.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
    it.setInput(this, "UTF-8")
    it.nextTag()
}
