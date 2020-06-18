package org.cru.godtools.xml.service

import androidx.annotation.AnyThread
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.kotlin.coroutines.MutexMap
import org.ccci.gto.android.common.kotlin.coroutines.withLock
import org.ccci.gto.android.common.support.v4.util.WeakLruCache
import org.ccci.gto.android.common.util.xmlpull.CloseableXmlPullParser
import org.cru.godtools.base.FileManager
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Page
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManifestParser @Inject internal constructor(private val fileManager: FileManager) {
    private val cache = WeakLruCache<String, Result.Data>(6)
    private val loadingMutex = MutexMap()

    @AnyThread
    suspend fun parse(manifestName: String, toolCode: String, locale: Locale) = loadingMutex.withLock(manifestName) {
        try {
            cache[manifestName] ?: withContext(Dispatchers.IO) {
                val manifest = try {
                    fileManager.getInputStream(manifestName)!!.buffered().xmlPullParser().use {
                        Manifest(toolCode, locale, it)
                    }
                } catch (e: FileNotFoundException) {
                    return@withContext Result.Error.NotFound
                } catch (e: XmlPullParserException) {
                    return@withContext Result.Error.Corrupted
                }

                // load pages
                val pageResults = manifest.pages.map { async { it.parse() } }.map { it.await() }
                if (pageResults.any { it is Result.Error.NotFound }) return@withContext Result.Error.Corrupted

                // return the result and store it in the cache
                Result.Data(manifest).also { cache.put(manifestName, it) }
            }
        } catch (e: IOException) {
            Result.Error()
        }
    }

    private fun Page.parse(): Result {
        val fileName = page.localFileName ?: return Result.Success
        return try {
            fileManager.getInputStream(fileName)!!.buffered().xmlPullParser().use { page.parsePageXml(it) }
            Result.Success
        } catch (e: FileNotFoundException) {
            Result.Error.NotFound
        }
    }
}

@VisibleForTesting
internal fun InputStream.xmlPullParser() = CloseableXmlPullParser().also {
    it.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
    it.setInput(this, "UTF-8")
    it.nextTag()
}

sealed class Result {
    class Data(val manifest: Manifest) : Result()

    internal object Success : Result()
    open class Error : Result() {
        object Corrupted : Error()
        object NotFound : Error()
    }
}
