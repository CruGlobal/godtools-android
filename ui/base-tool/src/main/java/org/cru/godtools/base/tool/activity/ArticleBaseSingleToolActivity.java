package org.cru.godtools.base.tool.activity;

import org.cru.godtools.xml.model.Manifest;

import androidx.annotation.NonNull;

public class ArticleBaseSingleToolActivity extends BaseSingleToolActivity {

    public ArticleBaseSingleToolActivity(final boolean immersive) {
        super(immersive);
    }

    public ArticleBaseSingleToolActivity(final boolean immersive, final boolean requiretool) {
        super(immersive, requiretool);
    }

    @Override
    protected boolean isSupportedType(@NonNull final Manifest.Type type) {
        return type == Manifest.Type.ARTICLE;
    }
}
