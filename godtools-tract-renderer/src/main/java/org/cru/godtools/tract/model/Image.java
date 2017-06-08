package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import butterknife.ButterKnife;

import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;

public final class Image extends Content {
    static final String XML_IMAGE = "image";
    private static final String XML_RESOURCE = "resource";

    @Nullable
    private String mResource = null;

    private Image(@NonNull final Base parent) {
        super(parent);
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
    View render(@NonNull final LinearLayout parent) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tract_content_image, parent, false);
        Resource.bind(getResource(mResource), ButterKnife.findById(view, R.id.content));
        return view;
    }
}
