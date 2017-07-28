# New Relic settings
-keep class com.newrelic.** { *; }
-dontwarn com.newrelic.**
-keepattributes Exceptions, Signature, InnerClasses, LineNumberTable


## New Relic network library support
# See: https://docs.newrelic.com/docs/mobile-monitoring/new-relic-mobile-android/install-configure/configure-proguard-or-dexguard-android-apps

# New Relic - OkHttp3
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**


# New Relic - Retrofit 2
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
