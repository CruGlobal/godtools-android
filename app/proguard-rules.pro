-optimizationpasses 10

# Strip built-in logging
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}


# Okio
-dontwarn okio.**


## Picasso
-dontwarn com.squareup.okhttp.**


#GooglePlay

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}
