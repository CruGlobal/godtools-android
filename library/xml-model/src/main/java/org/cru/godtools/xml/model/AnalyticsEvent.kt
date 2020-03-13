package org.cru.godtools.xml.model

import androidx.annotation.WorkerThread
import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.xml.XMLNS_ANALYTICS
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

private const val XML_EVENT = "event"
private const val XML_ACTION = "action"
private const val XML_DELAY = "delay"
private const val XML_SYSTEM = "system"
private const val XML_SYSTEM_ADOBE = "adobe"
private const val XML_SYSTEM_APPSFLYER = "appsflyer"
private const val XML_SYSTEM_FACEBOOK = "facebook"
private const val XML_SYSTEM_FIREBASE = "firebase"
private const val XML_SYSTEM_SNOWPLOW = "snowplow"
private const val XML_TRIGGER = "trigger"
private const val XML_TRIGGER_SELECTED = "selected"
private const val XML_TRIGGER_VISIBLE = "visible"
private const val XML_TRIGGER_HIDDEN = "hidden"
private const val XML_ATTRIBUTE = "attribute"
private const val XML_ATTRIBUTE_KEY = "key"
private const val XML_ATTRIBUTE_VALUE = "value"

@OptIn(ExperimentalStdlibApi::class)
class AnalyticsEvent internal constructor(parser: XmlPullParser) {
    enum class Trigger {
        SELECTED, VISIBLE, HIDDEN, DEFAULT, UNKNOWN;

        companion object {
            internal fun parse(trigger: String?, defValue: Trigger) = when (trigger) {
                XML_TRIGGER_SELECTED -> SELECTED
                XML_TRIGGER_VISIBLE -> VISIBLE
                XML_TRIGGER_HIDDEN -> HIDDEN
                null -> defValue
                else -> UNKNOWN
            }
        }
    }

    init {
        parser.require(XmlPullParser.START_TAG, XMLNS_ANALYTICS, XML_EVENT)
    }

    val action: String? = parser.getAttributeValue(null, XML_ACTION)
    val delay = parser.getAttributeValue(null, XML_DELAY)?.toIntOrNull() ?: 0
    private val systems = parser.getAttributeValue(null, XML_SYSTEM)?.parseSystems().orEmpty()
    private val trigger = Trigger.parse(parser.getAttributeValue(null, XML_TRIGGER), Trigger.DEFAULT)
    val attributes: Map<String, String>

    init {
        // process any child elements
        attributes = buildMap<String, String> {
            parsingChildren@ while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue

                when (parser.namespace) {
                    XMLNS_ANALYTICS -> when (parser.name) {
                        XML_ATTRIBUTE -> {
                            put(
                                parser.getAttributeValue(null, XML_ATTRIBUTE_KEY).orEmpty(),
                                parser.getAttributeValue(null, XML_ATTRIBUTE_VALUE).orEmpty()
                            )
                            XmlPullParserUtils.skipTag(parser)
                            continue@parsingChildren
                        }
                    }
                }

                // skip unrecognized nodes
                XmlPullParserUtils.skipTag(parser)
            }
        }
    }

    fun isTriggerType(vararg types: Trigger) = types.contains(trigger)
    fun isForSystem(system: AnalyticsSystem) = systems.contains(system)

    companion object {
        // TODO: make internal
        const val XML_EVENTS = "events"

        @JvmStatic
        @WorkerThread
        @Throws(XmlPullParserException::class, IOException::class)
        fun fromEventsXml(parser: XmlPullParser): Collection<AnalyticsEvent> {
            parser.require(XmlPullParser.START_TAG, XMLNS_ANALYTICS, XML_EVENTS)

            return buildList {
                parsingChildren@ while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.eventType != XmlPullParser.START_TAG) continue

                    when (parser.namespace) {
                        XMLNS_ANALYTICS -> when (parser.name) {
                            XML_EVENT -> {
                                add(AnalyticsEvent(parser))
                                continue@parsingChildren
                            }
                        }
                    }

                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser)
                }
            }
        }
    }
}

private fun String.parseSystems() = REGEX_SEQUENCE_SEPARATOR.split(this).mapNotNullTo(mutableSetOf()) {
    when (it) {
        XML_SYSTEM_ADOBE -> AnalyticsSystem.ADOBE
        XML_SYSTEM_APPSFLYER -> AnalyticsSystem.APPSFLYER
        XML_SYSTEM_FACEBOOK -> AnalyticsSystem.FACEBOOK
        XML_SYSTEM_FIREBASE -> AnalyticsSystem.FIREBASE
        XML_SYSTEM_SNOWPLOW -> AnalyticsSystem.SNOWPLOW
        else -> null
    }
}
