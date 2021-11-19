plugins {
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
}

android {
    defaultConfig {
        vectorDrawables.useSupportLibrary = true

        javaCompileOptions {
            annotationProcessorOptions.arguments += "eventBusIndex" to "org.cru.godtools.tract.TractEventBusIndex"
        }
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    api(project(":ui:base-tool"))
    api(project(":ui:tutorial-renderer"))
    implementation(project(":library:api"))
    implementation(project(":library:base"))
    implementation(project(":library:db"))
    implementation(project(":library:model"))
    implementation(project(":library:sync"))

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    api(libs.gtoSupport.viewpager)
    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.androidx.fragment)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.androidx.viewpager2)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.eventbus)
    implementation(libs.gtoSupport.lottie)
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.picasso)
    implementation(libs.gtoSupport.recyclerview)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.godtoolsMpp.parser)
    implementation(libs.hilt)
    implementation(libs.lottie)
    implementation(libs.materialComponents)
    implementation(libs.picasso.transformations)
    implementation(libs.play.instantapps)
    implementation(libs.rtlViewpager)
    implementation(libs.splitties.fragmentargs)
    implementation(libs.tinder.statemachine)
    implementation(libs.weakdelegate)

    kapt(libs.dagger.compiler)
    kapt(libs.eventbus.annotationProcessor)
    kapt(libs.hilt.compiler)

    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.gtoSupport.testing.dagger)
    testImplementation(libs.gtoSupport.testing.picasso)
    testImplementation(libs.hilt.testing)
    testImplementation(libs.kotlin.coroutines.test)

    kaptTest(libs.androidx.databinding.compiler)
    kaptTest(libs.hilt.compiler)
}
