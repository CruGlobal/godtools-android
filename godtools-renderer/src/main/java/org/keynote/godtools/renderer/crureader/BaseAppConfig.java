package org.keynote.godtools.renderer.crureader;

public abstract class BaseAppConfig {
    public abstract String getCannotSendEmail();

    public abstract String getAllWebsitesString();

    public abstract String getOpenString();

    public abstract String getEmailString();

    public abstract String getSingleWebsiteAssistString();

    public abstract String getMultiWebsiteString();

    public abstract String getChooseYourEmailProvider();

    public abstract String getCopy();

    public abstract String getFollowupModalInputValidGeneric();

    public abstract void overLog(String s);

    public abstract void logException(Exception e);
}
