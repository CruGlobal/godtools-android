package org.cru.godtools.xml.model

import androidx.annotation.RestrictTo
import androidx.annotation.WorkerThread
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.xml.XMLNS_ANALYTICS
import org.xmlpull.v1.XmlPullParser
import timber.log.Timber

private const val TAG = "XmlAnalyticsEvent"

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
class AnalyticsEvent {
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

    val action: String?
    val delay: Int
    private val systems: Set<AnalyticsSystem>
    private val trigger: Trigger
    val attributes: Map<String, String>

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(
        action: String? = null,
        delay: Int = 0,
        systems: Set<AnalyticsSystem>? = null,
        attributes: Map<String, String>? = null
    ) {
        this.action = action
        this.delay = delay
        this.systems = systems.orEmpty()
        trigger = Trigger.DEFAULT
        this.attributes = attributes.orEmpty()
    }

    internal constructor(parent: Base, parser: XmlPullParser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_ANALYTICS, XML_EVENT)

        action = parser.getAttributeValue(null, XML_ACTION)
        delay = parser.getAttributeValue(null, XML_DELAY)?.toIntOrNull() ?: 0
        systems = parser.getAttributeValue(null, XML_SYSTEM)?.parseSystems().orEmpty()
        trigger = Trigger.parse(parser.getAttributeValue(null, XML_TRIGGER), Trigger.DEFAULT)
        attributes = buildMap {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue

                when (parser.namespace) {
                    XMLNS_ANALYTICS -> when (parser.name) {
                        XML_ATTRIBUTE -> put(
                            parser.getAttributeValue(null, XML_ATTRIBUTE_KEY).orEmpty(),
                            parser.getAttributeValue(null, XML_ATTRIBUTE_VALUE).orEmpty()
                        )
                    }
                }

                parser.skipTag()
            }
        }

        // Log a non-fatal warning if this is an adobe analytics event
        if (systems.contains(AnalyticsSystem.ADOBE)) {
            val manifest = parent.manifest
            Timber.tag(TAG).e(
                UnsupportedOperationException("XML Adobe Analytics Event"),
                "action: $action tool: ${manifest.code} locale: ${manifest.locale}"
            )
        }
    }

    fun isTriggerType(vararg types: Trigger) = types.contains(trigger)
    fun isForSystem(system: AnalyticsSystem) = systems.contains(system)

    companion object {
        internal const val XML_EVENTS = "events"

        @WorkerThread
        fun fromEventsXml(parent: Base, parser: XmlPullParser): Collection<AnalyticsEvent> {
            parser.require(XmlPullParser.START_TAG, XMLNS_ANALYTICS, XML_EVENTS)

            return buildList {
                parsingChildren@ while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.eventType != XmlPullParser.START_TAG) continue

                    when (parser.namespace) {
                        XMLNS_ANALYTICS -> when (parser.name) {
                            XML_EVENT -> add(AnalyticsEvent(parent, parser))
                            else -> parser.skipTag()
                        }
                        else -> parser.skipTag()
                    }
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
