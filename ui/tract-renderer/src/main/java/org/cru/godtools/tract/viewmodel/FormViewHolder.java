package org.cru.godtools.tract.viewmodel;

import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.ui.controller.ParentController;
import org.cru.godtools.xml.model.Form;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import butterknife.BindView;

@UiThread
public final class FormViewHolder extends ParentController<Form> {
    FormViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
        super(Form.class, parent, R.layout.tract_content_paragraph, parentViewHolder);
    }

    @BindView(R2.id.content)
    LinearLayout mContent;

    @NonNull
    @Override
    protected LinearLayout getContentContainer() {
        return mContent;
    }

    @Override
    protected boolean validate(@NonNull final Set<Event.Id> ids) {
        // XXX: right now we only validate if we have a followup:send event
        if (ids.contains(Event.Id.FOLLOWUP_EVENT)) {
            // perform actual validation
            return onValidate();
        }

        // default to default validation logic
        return super.validate(ids);
    }

    @Override
    protected boolean buildEvent(@NonNull final Event.Builder builder) {
        // we override the default event building process
        onBuildEvent(builder, true);
        return true;
    }
}
