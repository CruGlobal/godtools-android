package org.keynote.godtools.android.snuffy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.view.View;

import org.keynote.godtools.android.snuffy.model.GtPage;

import java.util.HashSet;
import java.util.Set;

public class SnuffyPage extends SnuffyLayout
{
    private static final String TAG = "SnuffyPage";

    @NonNull
    private final GtPage mModel;

    private final SimpleArrayMap<String, SnuffyPage> mChildPages = new SimpleArrayMap<>();

    public String mDescription;
    public String mThumbnail;
    public Activity mCallingActivity; // used to host AlertDialog

    @Nullable
    private View mCover;
    private final Set<Runnable> mModalRemovals = new HashSet<>();

    public SnuffyPage(@NonNull final Context context, @NonNull final GtPage model) {
        super(context);
        mModel = model;
        mCover = null;
    }

    @NonNull
    public GtPage getModel() {
        return mModel;
    }

    public void addChildPage(@NonNull final SnuffyPage page) {
        mChildPages.put(page.getModel().getId(), page);
    }

    @Nullable
    public SnuffyPage getChildPage(@NonNull final String id) {
        return mChildPages.get(id);
    }

    public void setCover(@NonNull final View cover) {
        mCover = cover;
        mCover.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                hideAllModals();
            }
        });
    }

    public void onEnterPage()
    {
        Log.d(TAG, "OnEnterPage");
        hideAllModals();
    }

    public void onExitPage()
    {
        Log.d(TAG, "OnExitPage");
        hideAllModals();
    }

    public void addModal(@NonNull final Runnable modalRemoval, final boolean fullyTransparent) {
        // Set up for active panel to be removed on click anywhere on screen or when user drags to a new page
        mModalRemovals.add(modalRemoval);
        if (mCover != null) {
            if (fullyTransparent) {
                mCover.setBackgroundColor(Color.TRANSPARENT);
            } else {
                mCover.setBackgroundColor(Color.argb(51, 0, 0, 0)); // IOS Uses black with alpha=0.2
            }
        }
        showCoverIfNecessary();
    }

    public void removeModal(@NonNull final Runnable modalRemoval) {
        mModalRemovals.remove(modalRemoval);
        hideCoverIfNecessary();
    }

    private void showCoverIfNecessary() {
        if (mCover != null && !mModalRemovals.isEmpty()) {
            mCover.setVisibility(View.VISIBLE);
            mCover.bringToFront();
        }
    }

    public void hideAllModals() {
        if (!mModalRemovals.isEmpty()) {
            final Handler handler = new Handler();

            for (final Runnable removal : mModalRemovals) {
                handler.post(removal);
            }
        }
        hideCoverIfNecessary();
    }

    private void hideCoverIfNecessary() {
        if (mCover != null && mModalRemovals.isEmpty()) {
            mCover.setVisibility(View.GONE);
        }
    }
}
