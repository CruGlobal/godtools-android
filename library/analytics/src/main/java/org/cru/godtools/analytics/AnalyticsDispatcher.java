package org.cru.godtools.analytics;

import android.content.Context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class AnalyticsDispatcher implements InvocationHandler {
    private final Set<AnalyticsService> mServices = new HashSet<>();

    private final AnalyticsService mProxy;

    private AnalyticsDispatcher(@NonNull final Context context) {
        mProxy = (AnalyticsService) Proxy.newProxyInstance(getClass().getClassLoader(),
                                                           new Class<?>[] {AnalyticsService.class}, this);

        addAnalyticsService(AdobeAnalyticsService.getInstance(context));
    }

    @Nullable
    private static AnalyticsDispatcher sInstance;

    @NonNull
    public static synchronized AnalyticsDispatcher getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new AnalyticsDispatcher(context.getApplicationContext());
        }

        return sInstance;
    }

    @NonNull
    static synchronized AnalyticsService getAnalyticsService(@NonNull final Context context) {
        return getInstance(context).mProxy;
    }

    public void addAnalyticsService(@NonNull final AnalyticsService service) {
        mServices.add(service);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        // invoke this method for each configured service
        for (final AnalyticsService service : mServices) {
            method.invoke(service, args);
        }

        return null;
    }
}
