## New Relic settings
# See: https://docs.newrelic.com/docs/mobile-monitoring/new-relic-mobile-android/install-configure/configure-proguard-or-dexguard-android-apps
-keep class com.newrelic.** { *; }
-dontwarn com.newrelic.**
-keepattributes Exceptions, Signature, InnerClasses, LineNumberTable, SourceFile, EnclosingMethod

# New Relic - OkHttp3
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# New Relic - Retrofit 2
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
