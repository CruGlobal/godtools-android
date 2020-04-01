package org.cru.godtools.tract.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.annimon.stream.Optional;

import org.ccci.gto.android.common.util.os.BundleUtils;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.base.tool.activity.ImmersiveActivity;
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.Tool;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.viewmodel.ModalViewHolder;
import org.cru.godtools.xml.model.Manifest;
import org.cru.godtools.xml.model.Modal;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;

import static androidx.core.app.ActivityOptionsCompat.makeCustomAnimation;
import static org.cru.godtools.base.Constants.EXTRA_LANGUAGE;
import static org.cru.godtools.base.Constants.EXTRA_TOOL;
import static org.cru.godtools.tract.Constants.EXTRA_MANIFEST_FILE_NAME;
import static org.cru.godtools.tract.Constants.EXTRA_MODAL;
import static org.cru.godtools.tract.Constants.EXTRA_PAGE;

public class ModalActivity extends ImmersiveActivity {
    @Nullable
    @BindView(R2.id.modal_root)
    View mModalView;

    @Nullable
    private /*final*/ String mManifestFileName = null;
    @Nullable
    /*final*/ String mTool = Tool.INVALID_CODE;
    @NonNull
    /*final*/ Locale mLocale = Language.INVALID_CODE;
    @Nullable
    private /*final*/ String mPageId;
    @Nullable
    private /*final*/ String mModalId;

    @Nullable
    private Modal mModal;
    @Nullable
    private ModalViewHolder mModalViewHolder;

    public static void start(@NonNull final Activity activity, @NonNull final String manifestFileName,
                             @NonNull final String toolCode, @NonNull final Locale locale, @NonNull final String page,
                             @NonNull final String modal) {
        final Bundle extras = new Bundle(4);
        extras.putString(EXTRA_MANIFEST_FILE_NAME, manifestFileName);
        extras.putString(EXTRA_TOOL, toolCode);
        BundleUtils.putLocale(extras, EXTRA_LANGUAGE, locale);
        extras.putString(EXTRA_PAGE, page);
        extras.putString(EXTRA_MODAL, modal);

        ContextCompat.startActivity(activity, new Intent(activity, ModalActivity.class).putExtras(extras),
                                    makeCustomAnimation(activity, R.anim.activity_fade_in, R.anim.activity_fade_out)
                                            .toBundle());
    }

    public ModalActivity() {
        super(true, R.layout.activity_modal);
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final Bundle extras = intent != null ? intent.getExtras() : null;
        if (extras != null) {
            mManifestFileName = extras.getString(EXTRA_MANIFEST_FILE_NAME, mManifestFileName);
            mTool = extras.getString(EXTRA_TOOL, mTool);
            mLocale = BundleUtils.getLocale(extras, EXTRA_LANGUAGE, mLocale);
            mPageId = extras.getString(EXTRA_PAGE, mPageId);
            mModalId = extras.getString(EXTRA_MODAL, mModalId);
        }

        // finish now if this activity is in an invalid state
        if (!validStartState()) {
            finish();
            return;
        }

        setupDataModel();
        startLoaders();
    }

    @Override
    @CallSuper
    public void onContentChanged() {
        super.onContentChanged();
        setupModalViewHolder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContentEvent(@NonNull final Event event) {
        checkForDismissEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /* END lifecycle */

    private boolean validStartState() {
        return mTool != null;
    }

    // region Data Model
    private LatestPublishedManifestDataModel mDataModel;

    private void setupDataModel() {
        mDataModel = (new ViewModelProvider(this)).get(LatestPublishedManifestDataModel.class);
        mDataModel.getToolCode().setValue(mTool);
        mDataModel.getLocale().setValue(mLocale);
    }
    // endregion Data Model

    private void startLoaders() {
        mDataModel.getManifest().observe(this, this::updateModal);
    }

    void updateModal(@Nullable final Manifest manifest) {
        mModal = Optional.ofNullable(manifest)
                .map(m -> m.findPage(mPageId))
                .map(p -> p.findModal(mModalId))
                .orElse(null);
        if (mModal == null) {
            finish();
        }
        updateModalViewHolder();
    }

    private void setupModalViewHolder() {
        if (mModalView != null) {
            mModalViewHolder = ModalViewHolder.forView(mModalView);
            updateModalViewHolder();
        }
    }

    private void updateModalViewHolder() {
        if (mModalViewHolder != null) {
            mModalViewHolder.bind(mModal);
        }
    }

    private void checkForDismissEvent(@NonNull final Event event) {
        if (mModal != null) {
            if (mModal.getDismissListeners().contains(event.id)) {
                finish();
            }
        }
    }
}
