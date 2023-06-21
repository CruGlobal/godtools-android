package org.cru.godtools.model;

import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

import org.hamcrest.Matcher;

public class LanguageMatchers {
    public static Matcher<Language> languageMatcher(Language language) {
        return allOf(
                hasProperty("id", equalTo(language.getId())),
                hasProperty("code", equalTo(language.getCode())),
                hasProperty("name", equalTo(language.getName()))
        );
    }
}
