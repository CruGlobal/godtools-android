package org.cru.godtools.xml.model

import android.graphics.Color
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.ccci.gto.android.common.util.xmlpull.CloseableXmlPullParser
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.XMLNS_ARTICLE
import org.cru.godtools.xml.XMLNS_MANIFEST
import org.cru.godtools.xml.model.tips.Tip
import org.xmlpull.v1.XmlPullParser
import java.util.Locale

private const val XML_MANIFEST = "manifest"
private const val XML_TYPE = "type"
private const val XML_TYPE_ARTICLE = "article"
private const val XML_TYPE_TRACT = "tract"
private const val XML_TITLE = "title"
private const val XML_NAVBAR_COLOR = "navbar-color"
private const val XML_NAVBAR_CONTROL_COLOR = "navbar-control-color"
private const val XML_CATEGORY_LABEL_COLOR = "category-label-color"
private const val XML_CATEGORIES = "categories"
private const val XML_PAGES = "pages"
private const val XML_PAGES_PAGE = "page"
private const val XML_PAGES_PAGE_FILENAME = "filename"
private const val XML_PAGES_PAGE_SRC = "src"
private const val XML_PAGES_AEM_IMPORT = "aem-import"
private const val XML_PAGES_AEM_IMPORT_SRC = "src"
private const val XML_RESOURCES = "resources"
private const val XML_TIPS = "tips"
private const val XML_TIPS_TIP = "tip"
private const val XML_TIPS_TIP_ID = "id"
private const val XML_TIPS_TIP_SRC = "src"

@ColorInt
private val DEFAULT_BACKGROUND_COLOR = Color.WHITE
private val DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ImageScaleType.FILL
private val DEFAULT_BACKGROUND_IMAGE_GRAVITY = ImageGravity.CENTER

@OptIn(ExperimentalStdlibApi::class)
class Manifest : BaseModel, Styles {
    companion object {
        @ColorInt
        val DEFAULT_PRIMARY_COLOR = Color.argb(255, 59, 164, 219)
        @ColorInt
        val DEFAULT_PRIMARY_TEXT_COLOR = Color.WHITE
        @ColorInt
        val DEFAULT_TEXT_COLOR = Color.argb(255, 90, 90, 90)
    }

    enum class Type {
        TRACT, ARTICLE, UNKNOWN;

        companion object {
            val DEFAULT = TRACT

            fun parseOrNull(value: String?) = when (value) {
                XML_TYPE_ARTICLE -> ARTICLE
                XML_TYPE_TRACT -> TRACT
                null -> null
                else -> UNKNOWN
            }
        }
    }

    val code: String
    val locale: Locale
    val type: Type

    @ColorInt
    override val primaryColor: Int
    @ColorInt
    override val primaryTextColor: Int
    @ColorInt
    override val textColor: Int

    @ColorInt
    val backgroundColor: Int
    private val _backgroundImage: String?
    val backgroundImage get() = getResource(_backgroundImage)
    internal val backgroundImageGravity: ImageGravity
    val backgroundImageScaleType: ImageScaleType

    @ColorInt
    val navBarColor: Int?
    @ColorInt
    val navBarControlColor: Int?

    @ColorInt
    internal val categoryLabelColor: Int?

    private val _title: Text?
    val title: String? get() = _title?.text

    val categories: List<Category>
    val pages: List<Page>
    val aemImports: List<Uri>

    @VisibleForTesting
    internal val resources: Map<String?, Resource>
    val tips: Map<String?, Tip>

    internal constructor(
        code: String,
        locale: Locale,
        parser: XmlPullParser,
        parseFile: (String) -> CloseableXmlPullParser
    ) : super() {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_MANIFEST)

        this.code = code
        this.locale = locale
        type = Type.parseOrNull(parser.getAttributeValue(null, XML_TYPE)) ?: Type.DEFAULT

        primaryColor = parser.getAttributeValueAsColorOrNull(XML_PRIMARY_COLOR) ?: DEFAULT_PRIMARY_COLOR
        primaryTextColor = parser.getAttributeValueAsColorOrNull(XML_PRIMARY_TEXT_COLOR) ?: DEFAULT_PRIMARY_TEXT_COLOR
        textColor = parser.getAttributeValueAsColorOrNull(XML_TEXT_COLOR) ?: DEFAULT_TEXT_COLOR

        backgroundColor = parser.getAttributeValueAsColorOrNull(XML_BACKGROUND_COLOR) ?: DEFAULT_BACKGROUND_COLOR
        _backgroundImage = parser.getAttributeValue(null, XML_BACKGROUND_IMAGE)
        backgroundImageGravity =
            parser.getAttributeValueAsImageGravity(XML_BACKGROUND_IMAGE_GRAVITY, DEFAULT_BACKGROUND_IMAGE_GRAVITY)
        backgroundImageScaleType = parser.getAttributeValueAsImageScaleTypeOrNull(XML_BACKGROUND_IMAGE_SCALE_TYPE)
            ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE

        navBarColor = parser.getAttributeValueAsColorOrNull(XML_NAVBAR_COLOR)
        navBarControlColor = parser.getAttributeValueAsColorOrNull(XML_NAVBAR_CONTROL_COLOR)

        categoryLabelColor = parser.getAttributeValueAsColorOrNull(XML_CATEGORY_LABEL_COLOR)

        // process any child elements
        var title: Text? = null
        var categoriesData: List<Category>? = null
        var pagesData: PagesData? = null
        var resourcesData: Map<String?, Resource>? = null
        var tipsData: List<Tip>? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                XMLNS_MANIFEST -> when (parser.name) {
                    XML_TITLE -> title = Text.fromNestedXml(this, parser, XMLNS_MANIFEST, XML_TITLE)
                    XML_CATEGORIES -> categoriesData = parseCategories(parser)
                    XML_PAGES -> pagesData = parsePages(parser, parseFile)
                    XML_RESOURCES -> resourcesData = parseResources(parser)
                    XML_TIPS -> tipsData = parser.parseTips(parseFile)
                    else -> parser.skipTag()
                }
                else -> parser.skipTag()
            }
        }
        _title = title
        aemImports = pagesData?.aemImports.orEmpty()
        categories = categoriesData.orEmpty()
        pages = pagesData?.pages.orEmpty()
        resources = resourcesData.orEmpty()
        tips = tipsData?.associateBy { it.id }.orEmpty()
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(
        code: String = "",
        locale: Locale = Locale.ENGLISH,
        type: Type = Type.DEFAULT,
        tips: ((Manifest) -> List<Tip>?)? = null
    ) : super() {
        this.code = code
        this.locale = locale
        this.type = type

        primaryColor = DEFAULT_PRIMARY_COLOR
        primaryTextColor = DEFAULT_PRIMARY_TEXT_COLOR
        textColor = DEFAULT_TEXT_COLOR

        backgroundColor = DEFAULT_BACKGROUND_COLOR
        _backgroundImage = null
        backgroundImageGravity = DEFAULT_BACKGROUND_IMAGE_GRAVITY
        backgroundImageScaleType = DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE

        navBarColor = null
        navBarControlColor = null

        categoryLabelColor = null

        _title = null
        aemImports = emptyList()
        categories = emptyList()
        pages = emptyList()
        resources = emptyMap()
        this.tips = tips?.invoke(this)?.associateBy { it.id }.orEmpty()
    }

    override val manifest get() = this
    override fun getResource(name: String?) = name?.let { resources[name] }

    fun findCategory(category: String?) = categories.firstOrNull { it.id == category }
    fun findPage(id: String?) = pages.firstOrNull { it.id.equals(id, ignoreCase = true) }
    fun findTip(id: String?) = tips[id]

    @WorkerThread
    private fun parseCategories(parser: XmlPullParser): List<Category> {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_CATEGORIES)

        return buildList {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue

                when (parser.namespace) {
                    XMLNS_MANIFEST -> when (parser.name) {
                        Category.XML_CATEGORY -> add(Category(this@Manifest, parser))
                        else -> parser.skipTag()
                    }
                    else -> parser.skipTag()
                }
            }
        }
    }

    private class PagesData {
        var pages: List<Page>? = null
        val aemImports = mutableListOf<Uri>()
    }

    @WorkerThread
    private fun parsePages(parser: XmlPullParser, parseFile: (String) -> CloseableXmlPullParser) = runBlocking {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_PAGES)

        // process any child elements
        val result = PagesData()
        result.pages = buildList {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue

                when (parser.namespace) {
                    XMLNS_MANIFEST -> when (parser.name) {
                        XML_PAGES_PAGE -> {
                            val fileName = parser.getAttributeValue(null, XML_PAGES_PAGE_FILENAME)
                            val srcFile = parser.getAttributeValue(null, XML_PAGES_PAGE_SRC)
                            val position = size
                            parser.skipTag()

                            if (srcFile != null)
                                add(async { parseFile(srcFile).use { Page(this@Manifest, position, fileName, it) } })
                        }
                        else -> parser.skipTag()
                    }
                    XMLNS_ARTICLE -> when (parser.name) {
                        XML_PAGES_AEM_IMPORT -> {
                            parser.getAttributeValueAsUriOrNull(XML_PAGES_AEM_IMPORT_SRC)
                                ?.let { result.aemImports.add(it) }
                            parser.skipTag()
                        }
                        else -> parser.skipTag()
                    }
                    else -> parser.skipTag()
                }
            }
        }.awaitAll()
        return@runBlocking result
    }

    @WorkerThread
    private fun parseResources(parser: XmlPullParser): Map<String?, Resource> {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_RESOURCES)

        return buildList {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue

                when (parser.namespace) {
                    XMLNS_MANIFEST -> when (parser.name) {
                        Resource.XML_RESOURCE -> add(Resource(this@Manifest, parser))
                        else -> parser.skipTag()
                    }
                    else -> parser.skipTag()
                }
            }
        }.associateBy { it.name }
    }

    @WorkerThread
    private fun XmlPullParser.parseTips(parseFile: (String) -> CloseableXmlPullParser) = runBlocking {
        buildList {
            while (next() != XmlPullParser.END_TAG) {
                if (eventType != XmlPullParser.START_TAG) continue

                when (namespace) {
                    XMLNS_MANIFEST -> when (name) {
                        XML_TIPS_TIP -> {
                            val id = getAttributeValue(null, XML_TIPS_TIP_ID)
                            val src = getAttributeValue(null, XML_PAGES_PAGE_SRC)
                            if (id != null && src != null)
                                add(async { parseFile(src).use { Tip(this@Manifest, id, it) } })
                        }
                    }
                }

                skipTag()
            }
        }.awaitAll()
    }
}

@get:ColorInt
val Manifest?.primaryColor get() = this?.primaryColor ?: Manifest.DEFAULT_PRIMARY_COLOR

@get:ColorInt
val Manifest?.navBarColor get() = this?.navBarColor ?: primaryColor
@get:ColorInt
val Manifest?.navBarControlColor get() = this?.navBarControlColor ?: primaryTextColor

@get:ColorInt
val Manifest?.backgroundColor get() = this?.backgroundColor ?: DEFAULT_BACKGROUND_COLOR
val Manifest?.backgroundImageGravity get() = this?.backgroundImageGravity ?: DEFAULT_BACKGROUND_IMAGE_GRAVITY
val Manifest?.backgroundImageScaleType get() = this?.backgroundImageScaleType ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE

@get:ColorInt
val Manifest?.categoryLabelColor get() = this?.categoryLabelColor ?: textColor
