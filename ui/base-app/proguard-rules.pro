# Don't obfuscate, this appears to trigger a bug in R8 <= 1.5.59
# see: https://issuetracker.google.com/issues/133167042
-dontobfuscate


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


#GooglePlay
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}
