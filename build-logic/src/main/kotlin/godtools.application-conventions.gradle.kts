plugins {
    id("com.android.application")
}

android {
    configureAndroidCommon(project)
    configureQaBuildType(project)
    configureFlavorDimensions(project)

    defaultConfig.targetSdk = 36
}

excludeAndroidSdkDependencies()
configureKtlint()
