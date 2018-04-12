package org.cru.godtools.analytics;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

final class DefaultAnalyticsService implements InvocationHandler {
    private final List<AnalyticsService> mServices = new ArrayList<>();

    private final AnalyticsService mProxy;
    private final EventBusAnalyticsHelper mEventBusHelper;

    private DefaultAnalyticsService(@NonNull final Context context) {
        mProxy = (AnalyticsService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {AnalyticsService.class}, this);
        mEventBusHelper = new EventBusAnalyticsHelper(mProxy);

        mServices.add(GoogleAnalyticsService.getInstance(context));
        mServices.add(AdobeAnalyticsService.getInstance(context));
        mServices.add(SnowplowAnalyticsService.getInstance(context));
    }

    @Nullable
    private static DefaultAnalyticsService sInstance;
    @NonNull
    static synchronized AnalyticsService getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new DefaultAnalyticsService(context.getApplicationContext());
        }

        return sInstance.mProxy;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        // invoke this method for each service
        for (final AnalyticsService service : mServices) {
            method.invoke(service, args);
        }

        return null;
    }
}
