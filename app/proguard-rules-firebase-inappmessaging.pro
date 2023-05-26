# transitive dependency through grpc is causing this
# this might not be necessary after https://github.com/grpc/grpc-java/issues/10152
-dontwarn com.google.protobuf.java_com_google_android_gmscore_sdk_target_granule__proguard_group_gtm_N1281923064GeneratedExtensionRegistryLite$Loader
