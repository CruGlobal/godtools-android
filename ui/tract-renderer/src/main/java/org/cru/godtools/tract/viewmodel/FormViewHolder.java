package org.cru.godtools.tract.viewmodel;

import android.view.ViewGroup;

import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R;
import org.cru.godtools.xml.model.Form;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

@UiThread
public final class FormViewHolder extends ParentViewHolder<Form> {
    FormViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
        super(Form.class, parent, R.layout.tract_content_paragraph, parentViewHolder);
    }

    @Override
    boolean validate(@NonNull final Set<Event.Id> ids) {
        // XXX: right now we only validate if we have a followup:send event
        if (ids.contains(Event.Id.FOLLOWUP_EVENT)) {
            // perform actual validation
            return onValidate();
        }

        // default to default validation logic
        return super.validate(ids);
    }

    @Override
    boolean buildEvent(@NonNull final Event.Builder builder) {
        // we override the default event building process
        onBuildEvent(builder, true);
        return true;
    }
}
