package org.cru.godtools.base.model

import java.util.Locale
import javax.annotation.concurrent.Immutable

class Event internal constructor(builder: Builder) {
    @JvmField
    val id = builder.id
    @JvmField
    val locale = builder.locale
    @JvmField
    val fields = builder.fields.toMap()

    @Immutable
    class Id internal constructor(val namespace: String, val name: String) {
        override fun equals(other: Any?) = other is Id &&
            namespace.equals(other.namespace, ignoreCase = true) &&
            name.equals(other.name, ignoreCase = true)

        override fun hashCode() =
            (namespace.toLowerCase(Locale.US).hashCode() * 31) + name.toLowerCase(Locale.US).hashCode()

        override fun toString() = "$namespace:$name"

        companion object {
            @JvmField
            val FOLLOWUP_EVENT = Id("followup", "send")

            @JvmStatic
            fun parse(defaultNamespace: String, raw: String?) = raw
                ?.split("\\s+".toRegex())
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
        lateinit var id: Id
        var locale: Locale? = null
        val fields = mutableMapOf<String, String>()

        fun id(id: Id): Builder {
            this.id = id
            return this
        }

        fun locale(locale: Locale): Builder {
            this.locale = locale
            return this
        }

        fun field(name: String, value: String): Builder {
            this.fields[name] = value
            return this
        }

        fun build() = Event(this)
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
