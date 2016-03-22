## Strip built-in logging
#-assumenosideeffects class android.util.Log {
#    public static boolean isLoggable(java.lang.String, int);
#    public static int v(...);
#    public static int i(...);
#    public static int w(...);
#    public static int d(...);
#    public static int e(...);
#}


# Crashlytics
-keepattributes SourceFile,LineNumberTable


# Google Guava
-dontwarn javax.annotation.**
-dontwarn sun.misc.Unsafe


## Google Play Services workaround for: https://code.google.com/p/android-developer-preview/issues/detail?id=3001
#-keep class com.google.android.gms.** { *; }
#-dontwarn com.google.android.gms.**


# newrelic settings
-keep class com.newrelic.** { *; }
-dontwarn com.newrelic.**
-keepattributes Exceptions, Signature, InnerClasses, LineNumberTable


## Picasso
-dontwarn com.squareup.okhttp.**
