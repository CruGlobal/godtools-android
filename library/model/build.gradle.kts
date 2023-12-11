plugins {
    id("godtools.library-conventions")
}

android.namespace = "org.cru.godtools.model"

dependencies {
    implementation(project(":library:base"))

    implementation(libs.androidx.annotation)

    implementation(libs.gtoSupport.jsonapi)

    testFixturesImplementation(libs.hamcrest)

    testImplementation(libs.json)
}
