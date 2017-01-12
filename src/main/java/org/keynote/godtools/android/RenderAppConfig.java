package org.keynote.godtools.android;

import org.keynote.godtools.renderer.crureader.BaseAppConfig;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;

/**
 * Created by rmatt on 1/4/2017.
 */

public class RenderAppConfig extends BaseAppConfig {
    @Override
    public String getCannotSendEmail() {
        return get(R.string.unable_to_send_the_email);
    }

    @Override
    public String getCannotDialString() {
        return get(R.string.cannot_dial);
    }

    @Override
    public String getCannotEmailString() {
        return get(R.string.cannot_email);
    }

    @Override
    public String getAllWebsitesString() {
        return get(R.string.all_websites);
    }

    @Override
    public String getOpenString() {
        return get(R.string.open);
    }

    @Override
    public String getCannotLaunchBrowserString() {
        return get(R.string.cannot_launch_browser);
    }

    @Override
    public String getEmailString() {
        return get(R.string.email);
    }

    @Override
    public String getSingleWebsiteAssistString() {
        return get(R.string.single_website_assist);
    }

    @Override
    public String getMultiWebsiteString() {
        return get(R.string.multi_website_assist);
    }

    @Override
    public String getChooseYourEmailProvider() {
        return get(R.string.choose_your_email_provider);
    }

    @Override
    public String getOK() {
        return get(R.string.ok);
    }

    @Override
    public String getCopy() {
        return get(R.string.copy);
    }

    @Override
    public String getFollowupModalInputValidGeneric() {
        return get(R.string.followup_modal_input_invalid_generic);
    }

    @Override
    public String getFollowupModalInputInvalidEmail() {
        return get(R.string.followup_modal_input_invalid_email);
    }

    public String get(int id) {
        return RenderSingleton.getInstance().getContext().getString(id);
    }
}
