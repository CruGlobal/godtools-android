package org.cru.godtools.xml.model

import java.util.Locale

class Event private constructor(builder: Builder) {
    val id = builder.id
    val locale = builder.locale
    val fields = builder.fields.toMap()

    class Builder {
        internal lateinit var id: EventId
        internal var locale: Locale? = null
        internal val fields = mutableMapOf<String, String>()

        fun id(id: EventId) = apply { this.id = id }
        fun locale(locale: Locale) = apply { this.locale = locale }
        fun field(name: String, value: String) = apply { fields[name] = value }

        fun build() = Event(this)
    }
}
