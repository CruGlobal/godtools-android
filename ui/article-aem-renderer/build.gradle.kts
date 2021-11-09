plugins {
    id("dagger.hilt.android.plugin")
}

android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions.arguments += mapOf(
                "room.schemaLocation" to file("room-schemas").toString(),
                "room.incremental" to "true"
            )
        }
    }
    buildFeatures.viewBinding = true

    sourceSets {
        named("test") { assets.srcDirs(file("room-schemas")) }
    }
}

dependencies {
    implementation(project(":library:base"))
    implementation(project(":library:db"))
    implementation(project(":library:model"))

    implementation(project(":ui:base-tool"))

    api(libs.androidx.room)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.room.ktx)

    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.androidx.room)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.okhttp3)
    implementation(libs.gtoSupport.retrofit2)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.godtoolsMpp.parser)
    implementation(libs.hilt)
    implementation(libs.retrofit2.converters.scalars)
    implementation(libs.jsoup)
    implementation(libs.okhttp3)
    implementation(libs.retrofit2)
    implementation(libs.splitties.fragmentargs)
    implementation(libs.weakdelegate)

    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.kotlin.coroutines.test)

    kapt(libs.androidx.room.compiler)
    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
}
