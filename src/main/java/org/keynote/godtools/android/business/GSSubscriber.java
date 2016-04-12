package org.keynote.godtools.android.business;

import org.ccci.gto.android.common.gson.GsonIgnore;

import java.util.Date;

/**
 * Created by dsgoers on 3/29/16.
 *
 * For subscribers to Growth Spaces
 */
public class GSSubscriber {

    @GsonIgnore
    private Integer id;
    private String routeId;
    private String languageCode;
    private String firstName;
    private String lastName;
    private String email;
    @GsonIgnore
    private Date createdTimestamp = new Date(System.currentTimeMillis());

    public GSSubscriber()
    {
    }

    public GSSubscriber(String routeId, String languageCode, String firstName, String lastName, String email)
    {
        this.routeId = routeId;
        this.languageCode = languageCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Date createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

}
