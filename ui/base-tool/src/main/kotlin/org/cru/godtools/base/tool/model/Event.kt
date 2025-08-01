package org.cru.godtools.base.tool.model

import io.fluidsonic.locale.toPlatform
import java.util.Locale
import org.cru.godtools.shared.tool.parser.model.EventId
import org.cru.godtools.shared.tool.parser.model.Manifest

class Event private constructor(builder: Builder) {
    val id = builder.id
    val tool = builder.tool
    val locale = builder.locale
    val fields = builder.fields.toMap()

    class Builder(manifest: Manifest? = null) {
        internal lateinit var id: EventId
        internal var tool: String? = manifest?.code
        internal var locale: Locale? = manifest?.locale?.toPlatform()
        internal val fields = mutableMapOf<String, String>()

        fun id(id: EventId) = apply { this.id = id }
        fun field(name: String, value: String) = apply { fields[name] = value }

        fun build() = Event(this)
    }
}
