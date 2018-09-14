# Keep unit tests in the main dex file
# see: https://issuetracker.google.com/u/0/issues/78108767
-keepclasseswithmembers class * {
    @org.junit.* *;
}
