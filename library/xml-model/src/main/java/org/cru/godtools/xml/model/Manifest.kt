package org.cru.godtools.xml.model

import android.graphics.Color
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.ccci.gto.android.common.util.xmlpull.CloseableXmlPullParser
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.XMLNS_ARTICLE
import org.cru.godtools.xml.XMLNS_MANIFEST
import org.cru.godtools.xml.model.lesson.LessonPage
import org.cru.godtools.xml.model.tips.Tip
import org.cru.godtools.xml.model.tract.TractPage
import org.xmlpull.v1.XmlPullParser

private const val XML_MANIFEST = "manifest"
private const val XML_TYPE = "type"
private const val XML_TYPE_ARTICLE = "article"
private const val XML_TYPE_LESSON = "lesson"
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

        internal val DEFAULT_BUTTON_STYLE = Button.Style.CONTAINED
    }

    enum class Type {
        TRACT, ARTICLE, LESSON, UNKNOWN;

        companion object {
            val DEFAULT = TRACT

            fun parseOrNull(value: String?) = when (value) {
                XML_TYPE_ARTICLE -> ARTICLE
                XML_TYPE_LESSON -> LESSON
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

    override val buttonStyle get() = DEFAULT_BUTTON_STYLE

    @ColorInt
    private val _navBarColor: Int?
    @get:ColorInt
    val navBarColor get() = _navBarColor ?: primaryColor
    @ColorInt
    private val _navBarControlColor: Int?
    @get:ColorInt
    val navBarControlColor get() = _navBarControlColor ?: primaryTextColor

    @ColorInt
    val backgroundColor: Int
    private val _backgroundImage: String?
    val backgroundImage get() = getResource(_backgroundImage)
    internal val backgroundImageGravity: ImageGravity
    val backgroundImageScaleType: ImageScaleType

    @ColorInt
    internal val categoryLabelColor: Int?

    private val _title: Text?
    val title: String? get() = _title?.text

    val categories: List<Category>
    val lessonPages: List<LessonPage>
    val tractPages: List<TractPage>
    val aemImports: List<Uri>

    @VisibleForTesting
    internal val resources: Map<String?, Resource>
    val tips: Map<String, Tip>

    internal constructor(
        code: String,
        locale: Locale,
        parser: XmlPullParser,
        parseFile: suspend (String) -> CloseableXmlPullParser
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

        _navBarColor = parser.getAttributeValueAsColorOrNull(XML_NAVBAR_COLOR)
        _navBarControlColor = parser.getAttributeValueAsColorOrNull(XML_NAVBAR_CONTROL_COLOR)

        categoryLabelColor = parser.getAttributeValueAsColorOrNull(XML_CATEGORY_LABEL_COLOR)

        // process any child elements
        var title: Text? = null
        val aemImports = mutableListOf<Uri>()
        val categories = mutableListOf<Category>()
        val lessonPages: List<LessonPage>
        val resources = mutableListOf<Resource>()
        val tips: List<Tip>
        val tractPages: List<TractPage>
        runBlocking {
            val lessonPageTasks = mutableListOf<Deferred<LessonPage>>()
            val tipsTasks = mutableListOf<Deferred<Tip>>()
            val tractPageTasks = mutableListOf<Deferred<TractPage>>()
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue

                when (parser.namespace) {
                    XMLNS_MANIFEST -> when (parser.name) {
                        XML_TITLE -> title = Text.fromNestedXml(this@Manifest, parser, XMLNS_MANIFEST, XML_TITLE)
                        XML_CATEGORIES -> categories += parser.parseCategories()
                        XML_PAGES -> {
                            val result = parser.parsePages(this, parseFile)
                            aemImports += result.aemImports
                            lessonPageTasks += result.lessonPageTasks
                            tractPageTasks += result.tractPageTasks
                        }
                        XML_RESOURCES -> resources += parser.parseResources()
                        XML_TIPS -> tipsTasks += parser.parseTips(this, parseFile)
                        else -> parser.skipTag()
                    }
                    else -> parser.skipTag()
                }
            }

            // await any deferred parsing
            tips = tipsTasks.awaitAll()
            lessonPages = lessonPageTasks.awaitAll()
            tractPages = tractPageTasks.awaitAll()
        }
        _title = title
        this.aemImports = aemImports
        this.categories = categories
        this.lessonPages = lessonPages
        this.resources = resources.associateBy { it.name }
        this.tips = tips.associateBy { it.id }
        this.tractPages = tractPages
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(
        code: String = "",
        locale: Locale = Locale.ENGLISH,
        type: Type = Type.DEFAULT,
        primaryColor: Int = DEFAULT_PRIMARY_COLOR,
        primaryTextColor: Int = DEFAULT_PRIMARY_TEXT_COLOR,
        navBarColor: Int? = null,
        navBarControlColor: Int? = null,
        tips: ((Manifest) -> List<Tip>?)? = null
    ) : super() {
        this.code = code
        this.locale = locale
        this.type = type

        this.primaryColor = primaryColor
        this.primaryTextColor = primaryTextColor
        textColor = DEFAULT_TEXT_COLOR

        backgroundColor = DEFAULT_BACKGROUND_COLOR
        _backgroundImage = null
        backgroundImageGravity = DEFAULT_BACKGROUND_IMAGE_GRAVITY
        backgroundImageScaleType = DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE

        _navBarColor = navBarColor
        _navBarControlColor = navBarControlColor

        categoryLabelColor = null

        _title = null
        aemImports = emptyList()
        categories = emptyList()
        lessonPages = emptyList()
        tractPages = emptyList()
        resources = emptyMap()
        this.tips = tips?.invoke(this)?.associateBy { it.id }.orEmpty()
    }

    override val manifest get() = this
    override fun getResource(name: String?) = name?.let { resources[name] }

    fun findCategory(category: String?) = categories.firstOrNull { it.id == category }
    fun findTractPage(id: String?) = tractPages.firstOrNull { it.id.equals(id, ignoreCase = true) }
    fun findTip(id: String?) = tips[id]

    @WorkerThread
    private fun XmlPullParser.parseCategories() = buildList {
        require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_CATEGORIES)

        while (next() != XmlPullParser.END_TAG) {
            if (eventType != XmlPullParser.START_TAG) continue

            when (namespace) {
                XMLNS_MANIFEST -> when (name) {
                    Category.XML_CATEGORY -> add(Category(this@Manifest, this@parseCategories))
                    else -> skipTag()
                }
                else -> skipTag()
            }
        }
    }

    private class PagesData {
        val aemImports = mutableListOf<Uri>()
        val lessonPageTasks = mutableListOf<Deferred<LessonPage>>()
        val tractPageTasks = mutableListOf<Deferred<TractPage>>()
    }

    @WorkerThread
    private fun XmlPullParser.parsePages(
        scope: CoroutineScope,
        parseFile: suspend (String) -> CloseableXmlPullParser
    ) = PagesData().also { result ->
        require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_PAGES)

        // process any child elements
        while (next() != XmlPullParser.END_TAG) {
            if (eventType != XmlPullParser.START_TAG) continue

            when (namespace) {
                XMLNS_MANIFEST -> when (name) {
                    XML_PAGES_PAGE -> {
                        val fileName = getAttributeValue(null, XML_PAGES_PAGE_FILENAME)
                        val src = getAttributeValue(null, XML_PAGES_PAGE_SRC)
                        skipTag()

                        if (src != null) {
                            @Suppress("NON_EXHAUSTIVE_WHEN")
                            when (type) {
                                Type.LESSON -> {
                                    val pos = result.lessonPageTasks.size
                                    result.lessonPageTasks += scope.async {
                                        parseFile(src).use { LessonPage(this@Manifest, pos, fileName, it) }
                                    }
                                }
                                Type.TRACT -> {
                                    val pos = result.tractPageTasks.size
                                    result.tractPageTasks += scope.async {
                                        parseFile(src).use { TractPage(this@Manifest, pos, fileName, it) }
                                    }
                                }
                            }
                        }
                    }
                    else -> skipTag()
                }
                XMLNS_ARTICLE -> when (name) {
                    XML_PAGES_AEM_IMPORT -> {
                        getAttributeValueAsUriOrNull(XML_PAGES_AEM_IMPORT_SRC)?.let { result.aemImports += it }
                        skipTag()
                    }
                    else -> skipTag()
                }
                else -> skipTag()
            }
        }
    }

    @WorkerThread
    private fun XmlPullParser.parseResources() = buildList {
        require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_RESOURCES)

        while (next() != XmlPullParser.END_TAG) {
            if (eventType != XmlPullParser.START_TAG) continue

            when (namespace) {
                XMLNS_MANIFEST -> when (name) {
                    Resource.XML_RESOURCE -> add(Resource(this@Manifest, this@parseResources))
                    else -> skipTag()
                }
                else -> skipTag()
            }
        }
    }

    @WorkerThread
    private fun XmlPullParser.parseTips(scope: CoroutineScope, parseFile: suspend (String) -> CloseableXmlPullParser) =
        buildList {
            while (next() != XmlPullParser.END_TAG) {
                if (eventType != XmlPullParser.START_TAG) continue

                when (namespace) {
                    XMLNS_MANIFEST -> when (name) {
                        XML_TIPS_TIP -> {
                            val id = getAttributeValue(null, XML_TIPS_TIP_ID)
                            val src = getAttributeValue(null, XML_TIPS_TIP_SRC)
                            if (id != null && src != null)
                                add(scope.async { parseFile(src).use { Tip(this@Manifest, id, it) } })
                        }
                    }
                }

                skipTag()
            }
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
