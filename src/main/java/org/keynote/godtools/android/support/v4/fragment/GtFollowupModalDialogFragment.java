package org.keynote.godtools.android.support.v4.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;

import org.ccci.gto.android.common.util.BundleCompat;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.keynote.godtools.android.SnuffyPWActivity;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.event.GodToolsEvent;
import org.keynote.godtools.android.snuffy.PackageManager;
import org.keynote.godtools.android.snuffy.model.GtFollowupModal;
import org.keynote.godtools.android.snuffy.model.GtManifest;
import org.keynote.godtools.android.snuffy.model.GtPage;
import org.keynote.godtools.android.snuffy.model.GtThankYou;

import java.util.concurrent.ExecutionException;

import static org.keynote.godtools.android.Constants.ARG_LANGUAGE;
import static org.keynote.godtools.android.Constants.ARG_MODAL_ID;
import static org.keynote.godtools.android.Constants.ARG_PACKAGE;
import static org.keynote.godtools.android.Constants.ARG_STATUS;

public class GtFollowupModalDialogFragment extends BottomSheetDialogFragment {
    // these properties should be treated as final and only set/modified in onCreate()
    @NonNull
    private /* final */ String mPackageCode = GTPackage.INVALID_CODE;
    @NonNull
    private /* final */ String mLanguage = "en";
    @NonNull
    private /* final */ String mStatus = GTPackage.STATUS_LIVE;
    private /* final */ String mModalId;

    private DBAdapter mDao;
    private PackageManager mPackages;

    @Nullable
    private GtFollowupModal mModal;

    public static Bundle buildArgs(@NonNull final String gtPackage, @NonNull final String language,
                                   @NonNull final String status, @Nullable final String modalId) {
        final Bundle args = new Bundle();
        args.putString(ARG_PACKAGE, gtPackage);
        args.putString(ARG_LANGUAGE, language);
        args.putString(ARG_STATUS, status);
        args.putString(ARG_MODAL_ID, modalId);
        return args;
    }

    public static GtFollowupModalDialogFragment newInstance(@NonNull final String gtPackage,
                                                            @NonNull final String language,
                                                            @NonNull final String status,
                                                            @Nullable final String modalId) {
        final GtFollowupModalDialogFragment fragment = new GtFollowupModalDialogFragment();
        fragment.setArguments(buildArgs(gtPackage, language, status, modalId));
        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDao = DBAdapter.getInstance(getContext());
        mPackages = PackageManager.getInstance(getContext());

        final Bundle args = this.getArguments();
        if (args != null) {
            mPackageCode = BundleCompat.getString(args, ARG_PACKAGE, mPackageCode);
            mLanguage = BundleCompat.getString(args, ARG_LANGUAGE, mLanguage);
            mStatus = BundleCompat.getString(args, ARG_STATUS, mStatus);
            mModalId = BundleCompat.getString(args, ARG_MODAL_ID, mModalId);
        }

        // load GtFollowupModal
        loadModal();
        if (mModal == null) {
            dismissAllowingStateLoss();
        }

        // register with EventBus
        EventBus.getDefault().register(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);

        assert mModal != null;
        final GtFollowupModal.ViewHolder holder = mModal.render(getContext(), null, false);
        if (holder != null) {
            dialog.setContentView(holder.mRoot);
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(@NonNull final DialogInterface d) {
                    if (d instanceof Dialog) {
                        final View bottomSheet =
                                ((Dialog) d).findViewById(android.support.design.R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                }
            });
        } else {
            dismissAllowingStateLoss();
        }

        return dialog;
    }

    @Subscribe
    public void onGodToolsEvent(@NonNull final GodToolsEvent event) {
        if (mModal != null) {
            for (final GtThankYou thankYou : mModal.getThankYous()) {
                if (thankYou.getListeners().contains(event.getEventID())) {
                    final Activity activity = getActivity();
                    if (activity instanceof SnuffyPWActivity) {
                        ((SnuffyPWActivity) activity).onShowChildPage(thankYou.getId());
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /* END lifecycle */

    private void loadModal() {
        // XXX: lots of blocking operations here, probably should do this on a background thread at some point
        final GTPackage gtPackage = mDao.find(GTPackage.class, mLanguage, mStatus, mPackageCode);
        if (gtPackage != null) {
            try {
                final GtManifest manifest = mPackages.getManifest(gtPackage, false).get();
                if (manifest != null) {
                    for (final GtPage page : manifest.getPages()) {
                        final GtFollowupModal modal = page.getFollowupModal(mModalId);
                        if (modal != null) {
                            mModal = modal;
                            return;
                        }
                    }
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (final ExecutionException ignored) {
            }
        }
    }
}
