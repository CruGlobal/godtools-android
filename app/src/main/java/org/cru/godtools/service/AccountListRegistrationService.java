package org.cru.godtools.service;

import android.content.Context;
import android.os.AsyncTask;

import org.cru.godtools.api.GodToolsApi;
import org.cru.godtools.base.Settings;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import me.thekey.android.Attributes;
import me.thekey.android.TheKey;
import me.thekey.android.eventbus.event.LoginEvent;
import retrofit2.Response;
import timber.log.Timber;

import static org.cru.godtools.api.BuildConfig.CAMPAIGN_FORMS_ID;

public class AccountListRegistrationService {
    @NonNull
    private final GodToolsApi mApi;
    @NonNull
    private final TheKey mTheKey;
    @NonNull
    private final Settings mPrefs;

    private AccountListRegistrationService(@NonNull final Context context) {
        mApi = GodToolsApi.getInstance(context);
        mTheKey = TheKey.getInstance(context);
        mPrefs = Settings.getInstance(context);

        // register with EventBus
        EventBus.getDefault().register(this);

        // add the current user to Adobe Campaigns if they haven't been added already
        final String guid = mTheKey.getDefaultSessionGuid();
        if (guid != null && !mPrefs.isAddedToCampaign(guid)) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> registerUser(mTheKey.getDefaultSessionGuid()));
        }
    }

    private static AccountListRegistrationService sInstance;

    public static AccountListRegistrationService start(@NonNull final Context context) {
        synchronized (AccountListRegistrationService.class) {
            if (sInstance == null) {
                sInstance = new AccountListRegistrationService(context.getApplicationContext());
            }

            return sInstance;
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onLogin(@NonNull final LoginEvent event) {
        registerUser(event.getGuid());
    }

    @WorkerThread
    private void registerUser(@NonNull final String guid) {
        // short-circuit if we don't have valid attributes or the email is null
        final Attributes attrs = mTheKey.getAttributes(guid);
        if (!attrs.areValid() || attrs.getEmail() == null) {
            return;
        }

        // trigger the signup
        try {
            final Response<JSONObject> response = mApi.campaignForms
                    .signup(CAMPAIGN_FORMS_ID, attrs.getEmail(), attrs.getFirstName(), attrs.getLastName())
                    .execute();
            if (response.isSuccessful()) {
                mPrefs.setAddedToCampaign(guid, true);
            }
        } catch (IOException e) {
            Timber.tag("AccountListRegService")
                    .d(e, "error registering user in account list");
        }

        // TODO: if this failed due to no connectivity, we should retry when we have an internet connection
    }
}
