android {
    defaultConfig {
        consumerProguardFiles += file("proguard-rules-snowplow.pro")

        javaCompileOptions {
            annotationProcessorOptions.arguments +=
                "eventBusIndex" to "org.cru.godtools.analytics.AnalyticsEventBusIndex"
        }

        buildConfigField("String", "APPSFLYER_DEV_KEY", "\"QdbVaVHi9bHRchUTWtoaij\"")
        buildConfigField("String", "SNOWPLOW_ENDPOINT", "\"s.cru.org\"")
    }

    buildTypes {
        debug {
            resValue("string", "facebook_app_id", "448969905944197")
            buildConfigField("String", "SNOWPLOW_APP_ID", "\"godtools-dev\"")
        }
        release {
            resValue("string", "facebook_app_id", "2236701616451487")
            buildConfigField("String", "SNOWPLOW_APP_ID", "\"godtools\"")
        }
    }
}

dependencies {
    api(project(":library:base"))

    implementation(libs.kotlin.coroutines)

    implementation(libs.firebase.core)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.perf)

    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.okta)
    implementation(libs.gtoSupport.snowplow)

    implementation(libs.appsflyer)
    implementation(libs.dagger)
    implementation(libs.eventbus)
    implementation(libs.facebook)
    implementation(libs.hilt)
    implementation(libs.okhttp3)
    implementation(libs.play.installreferrer)
    implementation(libs.play.tagmanager)
    implementation(libs.snowplow)
    implementation(libs.weakdelegate)

    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.play.billing)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
    kapt(libs.eventbus.annotationProcessor)
}
