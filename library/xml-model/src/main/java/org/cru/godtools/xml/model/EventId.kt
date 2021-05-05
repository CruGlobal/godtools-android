package org.cru.godtools.xml.model

import androidx.annotation.VisibleForTesting
import java.util.Locale

data class EventId @VisibleForTesting internal constructor(val namespace: String, val name: String) {
    override fun equals(other: Any?) = other is EventId &&
        namespace.equals(other.namespace, ignoreCase = true) &&
        name.equals(other.name, ignoreCase = true)

    override fun hashCode() =
        (namespace.toLowerCase(Locale.ROOT).hashCode() * 31) + name.toLowerCase(Locale.ROOT).hashCode()

    override fun toString() = "$namespace:$name"

    companion object {
        val FOLLOWUP_EVENT = EventId("followup", "send")

        fun parse(defaultNamespace: String, raw: String?) = raw
            ?.split(Regex("\\s+"))
            ?.mapNotNull {
                val components = it.split(':', limit = 2)
                when {
                    it.isEmpty() -> null
                    components.size == 1 -> EventId(defaultNamespace, it)
                    else -> EventId(components[0], components[1])
                }
            }
            ?.toSet().orEmpty()
    }
}
