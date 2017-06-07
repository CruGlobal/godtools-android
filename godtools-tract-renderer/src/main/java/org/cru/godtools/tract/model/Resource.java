package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.view.View;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import org.ccci.gto.android.common.picasso.view.PicassoImageView;
import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView;
import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_TOP;
import static android.widget.RelativeLayout.CENTER_HORIZONTAL;
import static android.widget.RelativeLayout.CENTER_VERTICAL;
import static org.cru.godtools.base.util.FileUtils.getFile;
import static org.cru.godtools.tract.Constants.XMLNS_MANIFEST;
import static org.cru.godtools.tract.compat.RelativeLayoutCompat.ALIGN_PARENT_END;
import static org.cru.godtools.tract.compat.RelativeLayoutCompat.ALIGN_PARENT_START;

public final class Resource extends Base {
    static final String XML_RESOURCE = "resource";
    private static final String XML_FILENAME = "filename";
    private static final String XML_SRC = "src";

    @Nullable
    private String mName;
    @Nullable
    private String mLocalName;

    @NonNull
    @WorkerThread
    static Resource fromXml(@NonNull final Manifest manifest, @NonNull final XmlPullParser parser)
            throws XmlPullParserException, IOException {
        final Resource resource = new Resource(manifest);
        resource.parse(parser);
        return resource;
    }

    private Resource(@NonNull final Manifest manifest) {
        super(manifest);
    }

    @Nullable
    public String getName() {
        return mName;
    }

    @WorkerThread
    private void parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_RESOURCE);

        mName = parser.getAttributeValue(null, XML_FILENAME);
        mLocalName = parser.getAttributeValue(null, XML_SRC);

        // discard any nested nodes
        XmlPullParserUtils.skipTag(parser);
    }

    public static void bind(@Nullable final Resource resource, @Nullable final PicassoImageView view) {
        if (view != null) {
            view.setPicassoFile(resource != null ? getFile(view.getContext(), resource.mLocalName) : null);
        }
    }

    static void bindBackgroundImage(@Nullable final Resource resource, @NonNull final ImageScale scale, final int align,
                                    @NonNull final SimplePicassoImageView view) {
        // set the background image visibility
        view.setVisibility(resource != null ? View.VISIBLE : View.GONE);

        // update the background image itself
        bind(resource, view);
        final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();

        // set default layout for background image first
        view.setScaleType(ScaleType.CENTER_CROP);
        lp.addRule(ALIGN_PARENT_START, 0);
        lp.addRule(ALIGN_PARENT_END, 0);
        lp.addRule(CENTER_HORIZONTAL, 0);
        lp.addRule(ALIGN_PARENT_TOP, 0);
        lp.addRule(ALIGN_PARENT_BOTTOM, 0);
        lp.addRule(CENTER_VERTICAL, 0);

        // update scale type and alignment
        switch (scale) {
            case FIT:
                view.setScaleType(ScaleType.CENTER_INSIDE);
                break;
            case FILL:
                view.setScaleType(ScaleType.CENTER_CROP);
                break;
        }

        // update alignment (X-Axis)
        // TODO: RTL support
        if (ImageAlign.isStart(align)) {
            lp.addRule(ALIGN_PARENT_START);
        } else if (ImageAlign.isEnd(align)) {
            lp.addRule(ALIGN_PARENT_END);
        } else if (ImageAlign.isCenterX(align)) {
            lp.addRule(CENTER_HORIZONTAL);
        }

        // update alignment (Y-Axis)
        if (ImageAlign.isTop(align)) {
            lp.addRule(ALIGN_PARENT_TOP);
        } else if (ImageAlign.isBottom(align)) {
            lp.addRule(ALIGN_PARENT_BOTTOM);
        } else if (ImageAlign.isCenterY(align)) {
            lp.addRule(CENTER_VERTICAL);
        }

        // set the layout params back on the image
        view.setLayoutParams(lp);
    }
}
