package org.cru.godtools.model.jsonapi

import org.ccci.gto.android.common.jsonapi.converter.TypeConverter
import org.cru.godtools.model.Tool

object ToolTypeConverter : TypeConverter<Tool.Type> {
    override fun supports(clazz: Class<*>) = Tool.Type::class.java == clazz
    override fun toString(value: Tool.Type?) = value?.toJson()
    override fun fromString(value: String?) = Tool.Type.fromJson(value)
}
