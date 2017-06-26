package org.cru.godtools.api.model;

import org.keynote.godtools.android.business.GTLanguage;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

@Deprecated
@Root(name = "languages")
public class GTLanguages {
    @ElementList(inline = true, required = true, name = "languages", entry = "language", type = GTLanguage.class)
    public ArrayList<GTLanguage> mLanguages = new ArrayList<GTLanguage>();
}
