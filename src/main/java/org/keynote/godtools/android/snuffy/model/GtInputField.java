package org.keynote.godtools.android.snuffy.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Strings;

import org.apache.commons.lang3.StringUtils;
import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.keynote.godtools.android.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.ccci.gto.android.common.Constants.INVALID_STRING_RES;

public class GtInputField extends GtModel {
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_FIRST_NAME = "first_name";
    public static final String FIELD_LAST_NAME = "last_name";
    public static final String FIELD_NAME = "name";

    public enum Type {
        EMAIL, TEXT;

        public static Type DEFAULT = TEXT;

        private static final String XML_ATTR_TYPE_EMAIL = "email";
        private static final String XML_ATTR_TYPE_TEXT = "text";

        @NonNull
        static Type fromXmlAttr(@Nullable final String attr) {
            if (attr != null) {
                switch (attr) {
                    case XML_ATTR_TYPE_EMAIL:
                        return EMAIL;
                    case XML_ATTR_TYPE_TEXT:
                        return TEXT;
                }
            }

            return DEFAULT;
        }
    }

    static final String XML_INPUT_FIELD = "input-field";
    private static final String XML_LABEL = "input-label";
    private static final String XML_PLACEHOLDER = "input-placeholder";

    private static final String XML_ATTR_TYPE = "type";
    private static final String XML_ATTR_NAME = "name";
    private static final String XML_ATTR_VALID_FORMAT = "valid-format";

    @NonNull
    Type mType = Type.DEFAULT;
    @Nullable
    String mName;
    @Nullable
    Pattern mValidFormat;
    @Nullable
    String mLabel;
    @Nullable
    String mPlaceholder;

    private GtInputField(@NonNull final GtFollowupModal parent) {
        super(parent);
    }

    @NonNull
    public Type getType() {
        return mType;
    }

    public String getName() {
        return mName;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getPlaceholder() {
        return mPlaceholder;
    }

    boolean hasValidation() {
        return mValidFormat != null || mType == Type.EMAIL;
    }

    boolean isValidValue(@Nullable String value) {
        value = Strings.nullToEmpty(value);

        // handle explicit formats
        if (mValidFormat != null) {
            return mValidFormat.matcher(value).matches();
        }

        // handle any pre-defined type formats
        switch (mType) {
            case EMAIL:
                return Patterns.EMAIL_ADDRESS.matcher(value).matches();
        }

        // default to true
        return true;
    }

    @StringRes
    int getErrorResource() {
        if (mValidFormat != null) {
            return R.string.followup_modal_input_invalid_generic;
        } else if (mType == Type.EMAIL) {
            return R.string.followup_modal_input_invalid_email;
        } else {
            return INVALID_STRING_RES;
        }
    }

    @NonNull
    @Override
    public ViewHolder render(@NonNull Context context, @Nullable ViewGroup parent, boolean attachToRoot) {
        final LayoutInflater inflater = LayoutInflater.from(context);

        // inflate the raw view
        final View view = inflater.inflate(R.layout.gt_input_field, parent, false);
        if (parent != null && attachToRoot) {
            parent.addView(view);
        }

        return new ViewHolder(view);
    }

    @NonNull
    static GtInputField fromXml(@NonNull final GtFollowupModal modal, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        final GtInputField field = new GtInputField(modal);
        field.parse(parser);
        return field;
    }

    @NonNull
    private GtInputField parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, XML_INPUT_FIELD);

        mType = Type.fromXmlAttr(parser.getAttributeValue(null, XML_ATTR_TYPE));
        mName = parser.getAttributeValue(null, XML_ATTR_NAME);
        try {
            final String pattern = parser.getAttributeValue(null, XML_ATTR_VALID_FORMAT);
            if (pattern != null) {
                mValidFormat = Pattern.compile(pattern, CASE_INSENSITIVE);
            }
        } catch (final PatternSyntaxException e) {
            mValidFormat = null;
        }

        // loop until we reach the matching end tag for this element
        while (parser.next() != XmlPullParser.END_TAG) {
            // skip anything that isn't a start tag for an element
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized elements
            switch (parser.getName()) {
                case XML_LABEL:
                    mLabel = XmlPullParserUtils.safeNextText(parser);
                    break;
                case XML_PLACEHOLDER:
                    mPlaceholder = XmlPullParserUtils.safeNextText(parser);
                    break;
                default:
                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser);
            }
        }

        return this;
    }

    public class ViewHolder extends GtModel.ViewHolder {
        @Nullable
        @BindView(R.id.label)
        TextView mLabelView;
        @BindView(R.id.input)
        EditText mInputView;
        @Nullable
        @BindView(R.id.inputLayout)
        TextInputLayout mInputLayout;

        protected ViewHolder(@NonNull final View root) {
            super(root);
            ButterKnife.bind(this, root);

            setupLabel();
            setupInput();
        }

        /* BEGIN lifecycle */

        @OnTextChanged(value = R.id.input, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
        void onTextUpdated() {
            // only update if we are currently in an error state
            if (mInputLayout != null && mInputLayout.isErrorEnabled()) {
                onValidate(false);
            }
        }

        @OnFocusChange(R.id.input)
        void onFocusChanged(boolean hasFocus) {
            if (!hasFocus) {
                onValidate(false);
            }
        }

        @Override
        protected boolean onValidate(final boolean validateParent) {
            final int resId = getErrorResource();
            final boolean valid = isValidValue(mInputView.getText().toString());
            if (valid) {
                showError(null);
            } else if (resId != INVALID_STRING_RES) {
                showError(mRoot.getResources().getString(resId, StringUtils.capitalize(getName()), getValue()));
            } else {
                showError("");
            }

            return super.onValidate(validateParent) && valid;
        }

        /* END lifecycle */

        private void setupLabel() {
            if (mLabelView != null) {
                mLabelView.setVisibility(mLabel != null ? View.VISIBLE : View.GONE);
                mLabelView.setText(mLabel);
            }
        }

        private void setupInput() {
            // setup inputType
            if (mInputView != null) {
                int inputType = InputType.TYPE_CLASS_TEXT;
                switch (mType) {
                    case EMAIL:
                        inputType |= InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                        break;
                }
                mInputView.setInputType(inputType);
            }

            // setup the hint, prefer the label (unless we have a separate label view or no label is defined)
            String hint = mLabel;
            if (mLabelView != null || hint == null) {
                hint = mPlaceholder;
            }

            // set the hint on the layout or the actual input (based on what's available)
            if (mInputLayout != null) {
                mInputLayout.setHint(hint);
            } else if (mInputView != null) {
                mInputView.setHint(hint);
            }
        }

        private void showError(@Nullable final String error) {
            if (mInputLayout != null) {
                mInputLayout.setError(error);
                mInputLayout.setErrorEnabled(error != null);
            } else if (mInputView != null) {
                mInputView.setError(error);
            }
        }

        @Nullable
        public String getName() {
            return mName;
        }

        @Nullable
        public String getValue() {
            if (mInputView != null) {
                return mInputView.getText().toString();
            }

            return null;
        }
    }
}
