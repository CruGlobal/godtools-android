package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.ccci.gto.android.common.picasso.view.PicassoImageView;
import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.widget.ScaledPicassoImageView;
import org.cru.godtools.tract.widget.ScaledPicassoImageView.ScaleType;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal;
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical;

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

    static void bindBackgroundImage(@NonNull final ScaledPicassoImageView image, @Nullable final Resource resource,
                                    @NonNull final ScaleType scale, final int gravity) {
        image.toggleBatchUpdates(true);

        // set the background image visibility
        final ImageView view = image.asImageView();
        view.setVisibility(resource != null ? View.VISIBLE : View.GONE);

        // update the background image itself
        bind(resource, image);
        image.setScaleType(scale);
        // TODO: RTL support?
        image.setGravityHorizontal(ImageGravity.isStart(gravity) ? GravityHorizontal.LEFT :
                                           ImageGravity.isEnd(gravity) ? GravityHorizontal.RIGHT :
                                                   GravityHorizontal.CENTER);
        image.setGravityVertical(ImageGravity.isTop(gravity) ? GravityVertical.TOP :
                                         ImageGravity.isBottom(gravity) ? GravityVertical.BOTTOM :
                                                 GravityVertical.CENTER);

        image.toggleBatchUpdates(false);

        // update layout params
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof RelativeLayout.LayoutParams) {
            // set default layout for background image first
            final RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) lp;
            rlp.addRule(ALIGN_PARENT_START, 0);
            rlp.addRule(ALIGN_PARENT_END, 0);
            rlp.addRule(CENTER_HORIZONTAL, 0);
            rlp.addRule(ALIGN_PARENT_TOP, 0);
            rlp.addRule(ALIGN_PARENT_BOTTOM, 0);
            rlp.addRule(CENTER_VERTICAL, 0);

            // update gravity (X-Axis)
            // TODO: RTL support
            if (ImageGravity.isStart(gravity)) {
                rlp.addRule(ALIGN_PARENT_START);
            } else if (ImageGravity.isEnd(gravity)) {
                rlp.addRule(ALIGN_PARENT_END);
            } else if (ImageGravity.isCenterX(gravity)) {
                rlp.addRule(CENTER_HORIZONTAL);
            }

            // update gravity (Y-Axis)
            if (ImageGravity.isTop(gravity)) {
                rlp.addRule(ALIGN_PARENT_TOP);
            } else if (ImageGravity.isBottom(gravity)) {
                rlp.addRule(ALIGN_PARENT_BOTTOM);
            } else if (ImageGravity.isCenterY(gravity)) {
                rlp.addRule(CENTER_VERTICAL);
            }

            // set the layout params back on the image
            view.setLayoutParams(lp);
        }
    }
}
