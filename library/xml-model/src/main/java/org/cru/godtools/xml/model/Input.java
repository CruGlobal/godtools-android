package org.cru.godtools.xml.model;

import com.google.common.base.Strings;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.xml.R;
import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.WorkerThread;

import static org.ccci.gto.android.common.base.Constants.INVALID_STRING_RES;
import static org.cru.godtools.xml.Constants.XMLNS_CONTENT;
import static org.cru.godtools.xml.model.Utils.parseBoolean;

public final class Input extends Content {
    static final String XML_INPUT = "input";
    private static final String XML_TYPE = "type";
    private static final String XML_TYPE_TEXT = "text";
    private static final String XML_TYPE_EMAIL = "email";
    private static final String XML_TYPE_PHONE = "phone";
    private static final String XML_TYPE_HIDDEN = "hidden";
    private static final String XML_NAME = "name";
    private static final String XML_REQUIRED = "required";
    private static final String XML_VALUE = "value";
    private static final String XML_LABEL = "label";
    private static final String XML_PLACEHOLDER = "placeholder";

    private static final Pattern VALIDATE_EMAIL = Pattern.compile(".+@.+");

    public static class Error {
        @StringRes
        public final int msgId;
        @NonNull
        public final String msg;

        Error(@StringRes final int resId) {
            msgId = resId;
            msg = "Error!";
        }

        Error(@NonNull final String error) {
            msg = error;
            msgId = INVALID_STRING_RES;
        }
    }

    public enum Type {
        TEXT, EMAIL, PHONE, HIDDEN;

        public static final Type DEFAULT = TEXT;

        @Nullable
        @Contract("_,!null -> !null")
        static Type parse(@Nullable final String type, @Nullable final Type defValue) {
            switch (Strings.nullToEmpty(type)) {
                case XML_TYPE_EMAIL:
                    return EMAIL;
                case XML_TYPE_HIDDEN:
                    return HIDDEN;
                case XML_TYPE_PHONE:
                    return PHONE;
                case XML_TYPE_TEXT:
                    return TEXT;
            }

            return defValue;
        }
    }

    @NonNull
    Type mType = Type.DEFAULT;
    @Nullable
    String mName;
    @Nullable
    String mValue;

    boolean mRequired = false;

    @Nullable
    Text mLabel;
    @Nullable
    Text mPlaceholder;

    private Input(@NonNull final Base parent) {
        super(parent);
    }

    @NonNull
    public Type getType() {
        return mType;
    }

    @Nullable
    public String getName() {
        return mName;
    }

    @Nullable
    public String getValue() {
        return mValue;
    }

    @Nullable
    public Text getLabel() {
        return mLabel;
    }

    @Nullable
    public Text getPlaceholder() {
        return mPlaceholder;
    }

    @Nullable
    public Error validateValue(@Nullable final String raw) {
        final String value = Strings.nullToEmpty(raw);

        // check to see if the field is required
        if (mRequired) {
            if (value.trim().length() == 0) {
                return new Error(R.string.tract_content_input_error_required);
            }
        }

        // handle any pre-defined type formats
        switch (mType) {
            case EMAIL:
                // XXX: this pattern is too strict
                // Patterns.EMAIL_ADDRESS.matcher(value).matches();
                if (!VALIDATE_EMAIL.matcher(value).matches()) {
                    return new Error(R.string.tract_content_input_error_invalid_email);
                }
        }

        // default to no error
        return null;
    }

    @WorkerThread
    static Input fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Input(parent).parse(parser);
    }

    @WorkerThread
    private Input parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_INPUT);
        parseAttrs(parser);

        // process any child elements
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_CONTENT:
                    switch (parser.getName()) {
                        case XML_LABEL:
                            mLabel = Text.fromNestedXml(this, parser, XMLNS_CONTENT, XML_LABEL);
                            continue;
                        case XML_PLACEHOLDER:
                            mPlaceholder = Text.fromNestedXml(this, parser, XMLNS_CONTENT, XML_PLACEHOLDER);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }

        return this;
    }

    @Override
    protected void parseAttrs(@NonNull final XmlPullParser parser) {
        super.parseAttrs(parser);
        mType = Type.parse(parser.getAttributeValue(null, XML_TYPE), mType);
        mName = parser.getAttributeValue(null, XML_NAME);
        mValue = parser.getAttributeValue(null, XML_VALUE);
        mRequired = parseBoolean(parser.getAttributeValue(null, XML_REQUIRED), mRequired);
    }
}
