plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
}

android {
    namespace = "org.cru.godtools.analytics"

    configureCompose(project)
    createEventBusIndex("org.cru.godtools.analytics.AnalyticsEventBusIndex")

    defaultConfig {
        consumerProguardFiles += file("proguard-rules-snowplow.pro")

        buildConfigField("String", "APPSFLYER_DEV_KEY", "\"QdbVaVHi9bHRchUTWtoaij\"")
    }

    buildTypes {
        debug {
            resValue("string", "facebook_app_id", "448969905944197")
            resValue("string", "facebook_client_token", "be1edf48d86ed54a24951ededa62eda2")
        }
        release {
            resValue("string", "facebook_app_id", "2236701616451487")
            resValue("string", "facebook_client_token", "3b6bf5b7c128a970337c4fa1860ffa6e")
        }
    }
}

dependencies {
    api(project(":library:base"))
    implementation(project(":library:account"))
    implementation(project(":library:user-data"))
    implementation(project(":ui:base"))

    api(libs.godtoolsShared.analytics)

    implementation(libs.firebase.messaging)
    implementation(libs.firebase.perf)

    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.dagger)

    implementation(libs.appsflyer)
    implementation(libs.dagger)
    implementation(libs.eventbus)
    implementation(libs.facebook)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)
    implementation(libs.okhttp3)
    implementation(libs.play.installreferrer)
    implementation(libs.play.tagmanager)
    implementation(libs.weakdelegate)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
    kapt(libs.eventbus.annotationProcessor)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.coroutines.test)
}
