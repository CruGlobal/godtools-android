plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.db"

    configureCompose(project)

    testFixtures.enable = true

    sourceSets {
        named("test") { assets.srcDirs(file("room-schemas")) }
    }
}

ksp {
    arg("room.schemaLocation", file("room-schemas").toString())
    arg("room.incremental", "true")
}

dependencies {
    api(projects.library.model)
    implementation(projects.library.base)

    implementation(libs.androidx.room)
    implementation(libs.androidx.room.ktx)

    implementation(libs.gtoSupport.androidx.room)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.db)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.hilt)

    ksp(libs.androidx.room.compiler)
    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.gtoSupport.testing.androidx.room)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
}
