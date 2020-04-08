package org.cru.godtools.analytics.adobe

import com.adobe.mobile.Visitor
import com.google.firebase.perf.metrics.AddTrace

@get:AddTrace(name = "Visitor.getMarketingCloudId()")
internal val adobeMarketingCloudId get() = Visitor.getMarketingCloudId()
