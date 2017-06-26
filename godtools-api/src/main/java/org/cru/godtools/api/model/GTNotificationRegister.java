package org.cru.godtools.api.model;

@Deprecated
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
}
