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


# ButterKnife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}


# Crashlytics
-keepattributes SourceFile,LineNumberTable


# Google Guava
# http://stackoverflow.com/questions/9120338/proguard-configuration-for-guava-with-obfuscation-and-optimization
-dontwarn com.google.j2objc.annotations.Weak
-dontwarn java.lang.ClassValue
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
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


## Retrofit2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature,Exceptions
