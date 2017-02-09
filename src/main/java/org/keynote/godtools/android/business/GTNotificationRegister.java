package org.keynote.godtools.android.business;

/**
 * Created by rmatt on 2/8/2017.
 */

public class GTNotificationRegister {

    private String registrationId;
    private int notificationType;
    private boolean notificationsSent;

    public GTNotificationRegister() {
    }

    public GTNotificationRegister(String registrationId, int notificationType) {
        this.registrationId = registrationId;
        this.notificationType = notificationType;
        this.notificationsSent = false;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }

    public boolean isNotificationsSent() {
        return notificationsSent;
    }

    public void setNotificationsSent(boolean notificationsSent) {
        this.notificationsSent = notificationsSent;
    }

    //    jsonObject.put("id", null); // done by api
//    jsonObject.put("registrationId", objects[2].toString());
//    Log.i("registrationId", objects[2].toString());
//
//    jsonObject.put("notificationType", objects[3].toString());
//    Log.i("notificationType", objects[3].toString());
//
//    jsonObject.put("presentations", null); //done by api
//    jsonObject.put("notificationSent", false);
//    jsonObject.put("createdTimestamp", null); // done by api

}
