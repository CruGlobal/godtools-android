plugins {
    id("com.android.dynamic-feature")
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
configureKtlint()
