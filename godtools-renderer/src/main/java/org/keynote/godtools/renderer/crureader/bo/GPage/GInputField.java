package org.keynote.godtools.renderer.crureader.bo.GPage;

import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.keynote.godtools.renderer.crureader.R;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseTextAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;
import org.keynote.godtools.renderer.crureader.bo.GPage.Compat.RenderViewCompat;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.regex.Pattern;

@Root(name = "input-field")
public class GInputField extends GCoordinator {
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_FIRST_NAME = "first_name";
    public static final String FIELD_LAST_NAME = "last_name";
    public static final String FIELD_NAME = "name";

    private static final String TAG = "GInputField";
    @Attribute(name = "valid-format", required = false)
    public String validFormat;
    @Attribute
    public InputFieldType type;
    @Attribute
    public String name;
    @Element(name = "input-label", required = false)
    public GBaseTextAttributes inputLabel;
    @Element(name = "input-placeholder", required = false)
    public GInputPlaceholder inputPlaceholder;

    public void showError(TextInputLayout textInputLayout) {
        if (textInputLayout != null) {
            textInputLayout.setError(getErrorResource());
            textInputLayout.setErrorEnabled(getErrorResource() != null);
        } else if (textInputLayout != null) {
            textInputLayout.setError(getErrorResource());
        }
    }

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {

        View inflate = inflater.inflate(R.layout.g_input_field, viewGroup);
        final TextInputLayout textInputLayout = (TextInputLayout) inflate.findViewById(R.id.g_input_field_input_layout);
        textInputLayout.setId(RenderViewCompat.generateViewId());
        textInputLayout.setHint(inputLabel.content);
        textInputLayout.setTag(inflater.getContext().getString(R.string.scannable_text_input));

        final TextInputEditText textInputEditText = (TextInputEditText) inflate.findViewById(R.id.g_input_field_input_edit_text);
        textInputEditText.setTag(this);

        textInputEditText.setId(RenderViewCompat.generateViewId());
        updateBaseAttributes(textInputLayout);

        return textInputLayout.getId();
    }

    public boolean hasValidation() {

        return validFormat != null || type == InputFieldType.email;
    }

    public boolean isValidValue(@Nullable String value) {
        value = value == null ? "" : value;

        // handle explicit formats
        if (validFormat != null && !validFormat.isEmpty()) {
            Pattern pattern = Pattern.compile(validFormat);
            return pattern.matcher(value).matches();
        }

        // handle any pre-defined type formats
        switch (type) {
            case email:
                return Patterns.EMAIL_ADDRESS.matcher(value).matches();
        }

        // default to true
        return true;
    }

    public String getErrorResource() {
        if (validFormat != null) {
            return String.format(RenderSingleton.getInstance().getAppConfig().getFollowupModalInputValidGeneric(), name.toUpperCase()) ;
        } else if (type == InputFieldType.email) {
            return String.format(RenderSingleton.getInstance().getAppConfig().getFollowupModalInputValidGeneric(), name.toUpperCase()) ;
        } else {
            return "Invalid Error Resource";
        }
    }

    public enum InputFieldType {
        email, text
    }
}
