package org.cru.godtools.model;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;

import static org.cru.godtools.model.GlobalActivityAnalytics.JSON_API_TYPE;

@JsonApiType(JSON_API_TYPE)
public class GlobalActivityAnalytics extends Base {
    static final String JSON_API_TYPE = "global-activity-analytics";
    private static final String JSON_USERS = "users";
    private static final String JSON_COUNTRIES = "countries";
    private static final String JSON_LAUNCHES = "launches";
    private static final String JSON_GOSPEL_PRESENTATION = "gospel-presentations";

    @JsonApiAttribute(JSON_USERS)
    private int mUsers;

    @JsonApiAttribute(JSON_COUNTRIES)
    private int mCountries;

    @JsonApiAttribute(JSON_LAUNCHES)
    private int mLaunches;

    @JsonApiAttribute(JSON_GOSPEL_PRESENTATION)
    private int mGospelPresentation;

    public int getUsers() {
        return mUsers;
    }

    public void setUsers(final int users) {
        mUsers = users;
    }

    public int getCountries() {
        return mCountries;
    }

    public void setCountries(final int countries) {
        mCountries = countries;
    }

    public int getLaunches() {
        return mLaunches;
    }

    public void setLaunches(final int launches) {
        mLaunches = launches;
    }

    public int getGospelPresentation() {
        return mGospelPresentation;
    }

    public void setGospelPresentation(final int gospelPresentation) {
        mGospelPresentation = gospelPresentation;
    }
}
