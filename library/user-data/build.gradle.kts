plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

configureAndroidLibrary()

android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions.arguments +=
                "eventBusIndex" to "org.cru.godtools.user.data.UserEventBusIndex"
        }
    }
}

dependencies {
    implementation(project(":library:analytics"))
    implementation(project(":library:db"))
    implementation(project(":library:model"))
    implementation(project(":library:sync"))

    implementation(libs.gtoSupport.dagger)

    implementation(libs.dagger)
    implementation(libs.eventbus)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
    kapt(libs.eventbus.annotationProcessor)
}
