plugins {
    id("com.android.application")
}

android {
    configureAndroidCommon(project)
    configureQaBuildType(project)
    configureFlavorDimensions(project)

    defaultConfig.targetSdk = 37
}

excludeAndroidSdkDependencies()
configureKtlint()
