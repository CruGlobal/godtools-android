package org.keynote.godtools.android.snuffy.model;

public class Constants {
    public static final String NAMESPACE = "kgp";
    public static final GtManifest MANIFEST = new GtManifest(NAMESPACE, "en");
    public static final GtPage PAGE = new GtPage(MANIFEST, "test");
    public static final GtFollowupModal FOLLOWUP_MODAL = new GtFollowupModal(PAGE, "test");
}
