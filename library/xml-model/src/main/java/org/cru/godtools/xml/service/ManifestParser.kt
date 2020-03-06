package org.cru.godtools.xml.service

import android.content.Context
import android.util.Xml
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.kotlin.coroutines.MutexMap
import org.ccci.gto.android.common.kotlin.coroutines.withLock
import org.ccci.gto.android.common.support.v4.util.WeakLruCache
import org.cru.godtools.base.util.SingletonHolder
import org.cru.godtools.base.util.getGodToolsFile
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Page
import org.xmlpull.v1.XmlPullParser
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Locale

class ManifestParser private constructor(private val context: Context) {
    companion object : SingletonHolder<ManifestParser, Context>({ ManifestParser(it.applicationContext) })

    private val cache = WeakLruCache<String, Result.Data>(6)
    private val loadingMutex = MutexMap()

    @WorkerThread
    @Throws(InterruptedException::class)
    fun parseBlocking(manifestName: String, toolCode: String, locale: Locale) =
        runBlocking { parse(manifestName, toolCode, locale) }

    @AnyThread
    suspend fun parse(manifestName: String, toolCode: String, locale: Locale) = loadingMutex.withLock(manifestName) {
        try {
            cache[manifestName] ?: withContext(Dispatchers.IO) {
                val manifest = try {
                    context.getGodToolsFile(manifestName)!!.inputStream().buffered().use {
                        Manifest.fromXml(Xml.newPullParser().apply {
                            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
                            setInput(it, "UTF-8")
                            nextTag()
                        }, manifestName, toolCode, locale)
                    }
                } catch (e: FileNotFoundException) {
                    return@withContext Result.Error.NotFound
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
            context.getGodToolsFile(fileName)!!.inputStream().buffered().use {
                page.parsePageXml(Xml.newPullParser().apply {
                    setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
                    setInput(it, "UTF-8")
                    nextTag()
                })
            }
            Result.Success
        } catch (e: FileNotFoundException) {
            Result.Error.NotFound
        }
    }
}

sealed class Result {
    class Data(val manifest: Manifest) : Result()

    internal object Success : Result()
    open class Error : Result() {
        object Corrupted : Error()
        object NotFound : Error()
    }
}
