plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.db"

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    sourceSets {
        named("test") { assets.srcDirs(file("room-schemas")) }
    }
}

ksp {
    arg("room.schemaLocation", file("room-schemas").toString())
    arg("room.incremental", "true")
}

dependencies {
    api(project(":library:base"))
    implementation(project(":library:model"))

    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.room)
    implementation(libs.androidx.room.ktx)

    api(libs.gtoSupport.db)
    api(libs.gtoSupport.db.coroutines)
    api(libs.gtoSupport.db.livedata)
    implementation(libs.gtoSupport.androidx.collection)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.androidx.room)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.hilt)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    ksp(libs.androidx.room.compiler)

    testImplementation(kotlin("test"))
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(testFixtures(project(":library:model")))
    testImplementation(testFixtures(libs.gtoSupport.androidx.room))
}
