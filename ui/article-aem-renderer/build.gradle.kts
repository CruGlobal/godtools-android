plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.tool.article.aem"

    buildFeatures.viewBinding = true

    defaultConfig {
        consumerProguardFiles("src/main/proguard-jsoup.pro")
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
    implementation(projects.library.base)
    implementation(projects.library.db)
    implementation(projects.library.model)
    implementation(projects.ui.baseTool)

    api(libs.androidx.room)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.room.ktx)

    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.androidx.room)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.compat)
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

    ksp(libs.androidx.room.compiler)
    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.json)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(testFixtures(libs.gtoSupport.androidx.room))
}
