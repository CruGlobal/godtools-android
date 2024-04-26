package org.cru.godtools.shortcuts

import java.util.Locale

internal sealed interface ShortcutId {
    val id: String

    companion object {
        const val SEPARATOR = "|"

        fun parseId(id: String): ShortcutId? = when (id.substringBefore(SEPARATOR)) {
            Tool.TYPE -> Tool.parseId(id)
            else -> null
        }
    }

    data class Tool internal constructor(val tool: String, val locales: List<Locale>) : ShortcutId {
        internal constructor(tool: String, vararg locales: Locale?) : this(tool, locales.filterNotNull())

        override val id = (listOf(TYPE, tool) + locales.map { it.toLanguageTag() }).joinToString(SEPARATOR)

        val isFavoriteToolShortcut get() = locales.isEmpty()

        companion object {
            const val TYPE = "tool"

            fun parseId(id: String): Tool? {
                val components = id.split(SEPARATOR)
                return when {
                    components.size < 2 -> null
                    components[0] != TYPE -> null
                    else -> Tool(
                        tool = components[1],
                        locales = components.subList(2, components.size).map { Locale.forLanguageTag(it) }
                    )
                }
            }
        }
    }
}
