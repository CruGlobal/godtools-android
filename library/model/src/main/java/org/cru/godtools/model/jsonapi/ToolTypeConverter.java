package org.cru.godtools.model.jsonapi;

import org.ccci.gto.android.common.jsonapi.converter.TypeConverter;
import org.cru.godtools.model.Tool.Type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ToolTypeConverter implements TypeConverter<Type> {
    @Override
    public boolean supports(@NonNull final Class<?> clazz) {
        return Type.class.equals(clazz);
    }

    @Nullable
    @Override
    public String toString(@Nullable final Type value) {
        return value != null ? value.toJson() : null;
    }

    @Nullable
    @Override
    public Type fromString(@Nullable final String value) {
        return Type.fromJson(value);
    }
}
