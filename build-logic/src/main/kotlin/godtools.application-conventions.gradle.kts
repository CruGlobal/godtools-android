plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    configureAndroidCommon(project)
    configureQaBuildType(project)
    configureFlavorDimensions(project)
}

excludeAndroidSdkDependencies()
configureKtlint()
