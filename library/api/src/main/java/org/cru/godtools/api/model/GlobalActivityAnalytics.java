package org.cru.godtools.api.model;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;

import java.util.Date;

import static org.cru.godtools.api.model.GlobalActivityAnalytics.JSON_TYPE_GLOBAL_ACTIVITY_ANALYTICS;

@JsonApiType(JSON_TYPE_GLOBAL_ACTIVITY_ANALYTICS)
public class GlobalActivityAnalytics {
    static final String JSON_TYPE_GLOBAL_ACTIVITY_ANALYTICS = "global-activity-analytics";
    private static final String JSON_USERS = "users";
    private static final String JSON_COUNTRIES = "countries";
    private static final String JSON_LAUNCHES = "launches";
    private static final String JSON_GOSPEL_PRESENTATIONS = "gospel-presentations";

    @JsonApiAttribute(name = JSON_USERS)
    int users;

    @JsonApiAttribute(name = JSON_COUNTRIES)
    int countries;

    @JsonApiAttribute(name = JSON_LAUNCHES)
    int launches;

    @JsonApiAttribute(name = JSON_GOSPEL_PRESENTATIONS)
    int gospelPresentation;

    @JsonApiIgnore
    Date lastUpdated;

    public int getUsers() {
        return users;
    }

    public void setUsers(final int users) {
        this.users = users;
    }

    public int getCountries() {
        return countries;
    }

    public void setCountries(final int countries) {
        this.countries = countries;
    }

    public int getLaunches() {
        return launches;
    }

    public void setLaunches(final int launches) {
        this.launches = launches;
    }

    public int getGospelPresentation() {
        return gospelPresentation;
    }

    public void setGospelPresentation(final int gospelPresentation) {
        this.gospelPresentation = gospelPresentation;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(final Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
