package org.cru.godtools.api.model;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;

import java.util.Date;

import static org.cru.godtools.api.model.GlobalActivityAnalytics.JSON_TYPE_GLOBAL_ACTIVITY_ANALYTICS;

@JsonApiType(JSON_TYPE_GLOBAL_ACTIVITY_ANALYTICS)
public class GlobalActivityAnalytics {
    static final String JSON_TYPE_GLOBAL_ACTIVITY_ANALYTICS = "global-activity-analytics";
    private final String JSON_USERS = "users";
    public final String JSON_COUNTRIES = "countries";
    public final String JSON_LAUNCHES = "launches";
    public final String JSON_GOSPEL_PRESENTATIONS = "gospel-presentations";
    public final String TABLE_NAME_GLOBAL_ACTIVITY_ANALYTICS = "global_activity_analytics";

    @JsonApiAttribute(JSON_USERS)
    private int users;

    @JsonApiAttribute(JSON_COUNTRIES)
    private int countries;

    @JsonApiAttribute(JSON_LAUNCHES)
    private int launches;

    @JsonApiAttribute(JSON_GOSPEL_PRESENTATIONS)
    private int gospelPresentation;

    @JsonApiAttribute(TABLE_NAME_GLOBAL_ACTIVITY_ANALYTICS)
    private Date lastUpdated;

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
