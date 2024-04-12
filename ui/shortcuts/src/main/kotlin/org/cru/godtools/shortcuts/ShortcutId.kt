package org.cru.godtools.shortcuts

internal sealed interface ShortcutId {
    val id: String

    companion object {
        const val SEPARATOR = "|"

        fun parseId(id: String): ShortcutId? = when (id.substringBefore(SEPARATOR)) {
            Tool.TYPE -> Tool.parseId(id)
            else -> null
        }
    }

    data class Tool internal constructor(val tool: String) : ShortcutId {
        override val id = listOf(TYPE, tool).joinToString(SEPARATOR)

        companion object {
            const val TYPE = "tool"

            fun parseId(id: String): Tool? {
                val components = id.split(SEPARATOR)
                return when {
                    components.size < 2 -> null
                    components[0] != TYPE -> null
                    else -> Tool(components[1])
                }
            }
        }
    }
}
