package org.cru.godtools.base.tool.activity;

import org.cru.godtools.xml.model.Manifest;

public class ArticleBaseSingleToolActivity extends BaseSingleToolActivity {

    public ArticleBaseSingleToolActivity(final boolean immersive) {
        super(immersive);
    }

    public ArticleBaseSingleToolActivity(final boolean immersive, final boolean requiretool) {
        super(immersive, requiretool);
    }

    @Override
    protected int determineActiveToolState() {
        int state = super.determineActiveToolState();
        if (state == STATE_LOADED) {
            if (mManifest == null) {
                return STATE_NOT_FOUND;
            }
            return Manifest.Type.ARTICLE == mManifest.getType() ? state : STATE_NOT_FOUND;
        }
        return state;
    }
}
