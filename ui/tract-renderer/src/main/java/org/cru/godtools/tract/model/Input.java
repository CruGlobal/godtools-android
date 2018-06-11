package org.cru.godtools.tract.model;

import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.design.widget.TextInputLayout;
import android.support.design.widget.TextInputLayoutUtils;
import android.support.v4.view.ViewCompat;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Strings;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

import static org.ccci.gto.android.common.base.Constants.INVALID_STRING_RES;
import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;
import static org.cru.godtools.tract.model.Utils.parseBoolean;

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

    static class Error {
        @StringRes
        final int msgId;
        @NonNull
        final String msg;

        Error(@StringRes final int resId) {
            msgId = resId;
            msg = "Error!";
        }

        Error(@NonNull final String error) {
            msg = error;
            msgId = INVALID_STRING_RES;
        }
    }

    private enum Type {
        TEXT, EMAIL, PHONE, HIDDEN;

        static final Type DEFAULT = TEXT;

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

    @Nullable
    Error validateValue(@Nullable final String raw) {
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

        mType = Type.parse(parser.getAttributeValue(null, XML_TYPE), mType);
        mName = parser.getAttributeValue(null, XML_NAME);
        mValue = parser.getAttributeValue(null, XML_VALUE);
        mRequired = parseBoolean(parser.getAttributeValue(null, XML_REQUIRED), mRequired);

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

    @NonNull
    @Override
    InputViewHolder createViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
        return new InputViewHolder(parent, parentViewHolder);
    }

    @UiThread
    static final class InputViewHolder extends BaseViewHolder<Input> {
        @Nullable
        @BindView(R2.id.label)
        TextView mLabelView;
        @Nullable
        @BindView(R2.id.layout)
        TextInputLayout mInputLayout;
        @BindView(R2.id.input)
        EditText mInputView;

        @Nullable
        private Text mLabel;
        @Nullable
        private Text mPlaceholder;

        InputViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
            super(Input.class, parent, R.layout.tract_content_input, parentViewHolder);
        }

        @Override
        void onBind() {
            super.onBind();
            if (mModel != null) {
                mLabel = mModel.mLabel;
                mPlaceholder = mModel.mPlaceholder;
            } else {
                mLabel = null;
                mPlaceholder = null;
            }

            mRoot.setVisibility(mModel != null && mModel.mType == Type.HIDDEN ? View.GONE : View.VISIBLE);
            bindLabel();
            bindPlaceholder();
            bindInput();
        }

        /* BEGIN lifecycle */

        @OnTextChanged(value = R2.id.input, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
        void onTextUpdated() {
            // only update if we are currently in an error state
            if (mInputLayout != null && mInputLayout.isErrorEnabled()) {
                onValidate();
            }
        }

        @OnFocusChange(R2.id.input)
        void onFocusChanged(final boolean hasFocus) {
            if (!hasFocus) {
                onValidate();
            }
        }

        @Override
        boolean onValidate() {
            final String value = getValue();
            final Error error = mModel != null ? mModel.validateValue(value) : null;
            final String msg = error == null ? null : error.msgId != INVALID_STRING_RES ?
                    mRoot.getResources().getString(error.msgId, mModel.mName, value) : error.msg;
            showError(msg);
            return error == null;
        }

        @Override
        void onBuildEvent(@NonNull final Event.Builder builder, final boolean recursive) {
            if (mModel != null) {
                final String value = getValue();
                if (mModel.mName != null && value != null) {
                    builder.field(mModel.mName, value);
                }
            }
        }

        /* END lifecycle */

        private void bindLabel() {
            final Text labelStyles = mLabel != null ? mLabel : mPlaceholder;
            if (mInputLayout != null) {
                TextInputLayoutUtils.setCollapsedTextColor(mInputLayout, Text.getTextColor(labelStyles));
            }

        }

        private void bindPlaceholder() {
            // setup the hint, prefer the label (unless we have a separate label view or no label is defined)
            Text hintText = mLabel;
            if (mLabelView != null || hintText == null) {
                hintText = mPlaceholder;
            }

            // set the hint on the layout or the actual input (based on what's available)
            if (mInputLayout != null) {
                mInputLayout.setHint(Text.getText(hintText));
            } else if (mInputView != null) {
                mInputView.setHint(Text.getText(hintText));
            }

            // update placeholder styles
            final Text hintStyles = mPlaceholder != null ? mPlaceholder : mLabel;
            final int hintColor = Text.getTextColor(hintStyles);
            if (mInputLayout != null) {
                TextInputLayoutUtils.setExpandedTextColor(mInputLayout, hintColor);
            } else if (mInputView != null) {
                mInputView.setHintTextColor(hintColor);
            }
        }

        private void bindInput() {
            // setup inputType
            if (mInputView != null) {
                int inputType = InputType.TYPE_CLASS_TEXT;
                switch (mModel != null ? mModel.mType : Type.DEFAULT) {
                    case EMAIL:
                        inputType |= InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                        break;
                    case PHONE:
                        inputType = InputType.TYPE_CLASS_PHONE;
                        break;
                }
                mInputView.setInputType(inputType);
            }

            // style the input view
            final Styles stylesParent = getStylesParent(mModel);
            if (mInputView != null) {
                mInputView.setTextColor(Styles.getTextColor(stylesParent));
                ViewCompat.setBackgroundTintList(mInputView,
                                                 ColorStateList.valueOf(Styles.getPrimaryColor(stylesParent)));
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
        private String getValue() {
            if (mModel != null && mModel.mType == Type.HIDDEN) {
                return mModel.mValue;
            } else if (mInputView != null) {
                return mInputView.getText().toString();
            }

            return null;
        }
    }
}
