package org.keynote.godtools.android.business;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

/**
 * Created by rmatt on 2/7/2017.
 */
@Root(name = "languages")
public class GTLanguages {
    @ElementList(inline = true, required = true, name = "languages", entry = "language", type = GTLanguage.class)
    public ArrayList<GTLanguage> mLanguages = new ArrayList<GTLanguage>();
}
