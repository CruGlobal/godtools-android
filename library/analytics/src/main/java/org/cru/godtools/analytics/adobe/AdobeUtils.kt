package org.cru.godtools.analytics.adobe

import androidx.annotation.WorkerThread
import com.adobe.mobile.Visitor
import com.google.firebase.perf.metrics.AddTrace

@get:AddTrace(name = "Visitor.getMarketingCloudId()")
@get:WorkerThread
internal val adobeMarketingCloudId get() = Visitor.getMarketingCloudId()
