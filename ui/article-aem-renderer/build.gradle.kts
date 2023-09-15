plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.tool.article.aem"

    buildFeatures.viewBinding = true

    sourceSets {
        named("test") { assets.srcDirs(file("room-schemas")) }
    }
}

ksp {
    arg("room.schemaLocation", file("room-schemas").toString())
    arg("room.incremental", "true")
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
    implementation(libs.hilt)
    implementation(libs.retrofit2.converters.scalars)
    implementation(libs.jsoup)
    implementation(libs.okhttp3)
    implementation(libs.retrofit2)
    implementation(libs.splitties.fragmentargs)
    implementation(libs.weakdelegate)

    // TODO: transition to KSP for dagger once Data Binding is no longer used
    //       see: https://dagger.dev/dev-guide/ksp#interaction-with-javackapt-processors
    //       see: https://issuetracker.google.com/issues/173030256#comment10
    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    ksp(libs.androidx.room.compiler)

    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(testFixtures(libs.gtoSupport.androidx.room))
}
