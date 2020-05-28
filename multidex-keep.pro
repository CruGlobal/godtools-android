# Keep unit tests in the main dex file
# see: https://issuetracker.google.com/u/0/issues/78108767
-keepclasseswithmembers class * {
    @org.junit.* *;
}

# Keep direct dependencies in the main dex file
-keep class androidx.**
-keep class com.facebook.**
-keep class com.google.android.gms.**
-keep class com.google.firebase.**
-keep class com.squareup.picasso.**
-keep class kotlin.**
