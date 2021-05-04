package org.cru.godtools.xml.model

import androidx.annotation.VisibleForTesting
import java.util.Locale

class Event private constructor(builder: Builder) {
    val id = builder.id
    val locale = builder.locale
    val fields = builder.fields.toMap()

    data class Id @VisibleForTesting internal constructor(val namespace: String, val name: String) {
        override fun equals(other: Any?) = other is Id &&
            namespace.equals(other.namespace, ignoreCase = true) &&
            name.equals(other.name, ignoreCase = true)

        override fun hashCode() =
            (namespace.toLowerCase(Locale.ROOT).hashCode() * 31) + name.toLowerCase(Locale.ROOT).hashCode()

        override fun toString() = "$namespace:$name"

        companion object {
            val FOLLOWUP_EVENT = Id("followup", "send")

            fun parse(defaultNamespace: String, raw: String?) = raw
                ?.split(Regex("\\s+"))
                ?.mapNotNull {
                    val components = it.split(':', limit = 2)
                    when {
                        it.isEmpty() -> null
                        components.size == 1 -> Id(defaultNamespace, it)
                        else -> Id(components[0], components[1])
                    }
                }
                ?.toSet().orEmpty()
        }
    }

    class Builder {
        internal lateinit var id: Id
        internal var locale: Locale? = null
        internal val fields = mutableMapOf<String, String>()

        fun id(id: Id) = apply { this.id = id }
        fun locale(locale: Locale) = apply { this.locale = locale }
        fun field(name: String, value: String) = apply { fields[name] = value }

        fun build() = Event(this)
    }
}
