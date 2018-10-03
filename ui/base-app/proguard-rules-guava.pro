# Google Guava 25.0-android
# http://stackoverflow.com/questions/9120338/proguard-configuration-for-guava-with-obfuscation-and-optimization
-dontwarn com.google.errorprone.annotations.**
-dontwarn java.lang.ClassValue
-dontwarn afu.org.checkerframework.checker.**
-dontwarn org.checkerframework.checker.**
-dontwarn sun.misc.Unsafe

# HACK: workaround a proguard + D8 bug with lambdas implementing google guava functions
# see: https://issuetracker.google.com/issues/112297269
-keepnames class com.google.common.base.Function { *; }
