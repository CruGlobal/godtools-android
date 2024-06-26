plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.db"

    configureCompose(project)

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
    api(project(":library:model"))

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
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(testFixtures(libs.gtoSupport.androidx.room))
}
