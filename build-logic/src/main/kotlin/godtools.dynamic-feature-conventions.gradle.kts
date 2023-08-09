plugins {
    id("com.android.dynamic-feature")
    kotlin("android")
}

android {
    configureAndroidCommon(project)
    configureQaBuildType(project)
    configureFlavorDimensions(project)
}

dependencies {
    implementation(project(":app"))
}

excludeAndroidSdkDependencies()
