plugins {
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
}

android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions.arguments +=
                "eventBusIndex" to "org.cru.godtools.base.tool.BaseToolEventBusIndex"
        }

        vectorDrawables.useSupportLibrary = true
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    api(project(":library:analytics"))
    api(project(":library:db"))
    api(project(":library:download-manager"))
    api(project(":library:sync"))
    api(project(":ui:base"))
    implementation(project(":library:base"))
    implementation(project(":library:model"))

    implementation(libs.kotlin.coroutines)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.recyclerview)

    api(libs.gtoSupport.picasso)
    implementation(libs.gtoSupport.androidx.constraintlayout)
    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.animation)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.eventbus)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.lottie)
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.recyclerview)
    implementation(libs.gtoSupport.util)

    implementation(libs.godtoolsMpp.parser)

    api(libs.taptargetview)
    implementation(libs.dagger)
    implementation(libs.google.auto.value.annotations)
    implementation(libs.hilt)
    implementation(libs.lottie)
    implementation(libs.picasso)
    implementation(libs.picasso.transformations)
    implementation(libs.splitties.bitflags)
    implementation(libs.splitties.fragmentargs)
    implementation(libs.youtubePlayer)

    kapt(libs.dagger.compiler)
    kapt(libs.eventbus.annotationProcessor)
    kapt(libs.google.auto.value)
    kapt(libs.hilt.compiler)

    testImplementation(project(":ui:tract-renderer"))
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlin.coroutines.test)
    kaptTest(libs.androidx.databinding.compiler)
}
