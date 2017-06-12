package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.view.ViewGroup;

import org.ccci.gto.android.common.picasso.view.PicassoImageView;
import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.model.Parent.ParentViewHolder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import butterknife.BindView;

import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;

public final class Image extends Content {
    static final String XML_IMAGE = "image";
    private static final String XML_RESOURCE = "resource";

    @Nullable
    private String mResource = null;

    private Image(@NonNull final Base parent) {
        super(parent);
    }

    @Nullable
    static Resource getResource(@Nullable final Image image) {
        return image != null ? image.getResource(image.mResource) : null;
    }

    @WorkerThread
    static Image fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Image(parent).parse(parser);
    }

    @WorkerThread
    private Image parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_IMAGE);

        mResource = parser.getAttributeValue(null, XML_RESOURCE);

        XmlPullParserUtils.skipTag(parser);
        return this;
    }

    @NonNull
    @Override
    ImageViewHolder createViewHolder(@NonNull final ViewGroup parent,
                                     @Nullable final ParentViewHolder parentViewHolder) {
        return new ImageViewHolder(parent, parentViewHolder);
    }

    @UiThread
    static final class ImageViewHolder extends BaseViewHolder<Image> {
        @BindView(R2.id.content)
        PicassoImageView mImage;

        ImageViewHolder(@NonNull final ViewGroup parent, @Nullable final ParentViewHolder parentViewHolder) {
            super(Image.class, parent, R.layout.tract_content_image, parentViewHolder);
        }

        @Override
        void onBind() {
            super.onBind();
            Resource.bind(getResource(mModel), mImage);
        }
    }
}
