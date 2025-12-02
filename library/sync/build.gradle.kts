plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.ksp)
}

android.namespace = "org.cru.godtools.sync"

dependencies {
    implementation(projects.library.account)
    implementation(projects.library.api)
    implementation(projects.library.db)
    implementation(projects.library.model)

    implementation(libs.androidx.collection.ktx)
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.hilt.work)
    api(libs.androidx.work.ktx)

    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.kotlin.coroutines)

    implementation(libs.dagger)
    implementation(libs.eventbus)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)

    testImplementation(libs.kotlin.coroutines.test)

    ksp(libs.androidx.hilt.compiler)
    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)
}
