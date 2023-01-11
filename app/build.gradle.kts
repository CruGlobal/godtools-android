plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.firebase.appdistribution")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("com.google.gms.google-services")
    alias(libs.plugins.grgit)
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.cru.godtools"

    baseConfiguration(project)
    configureCompose(project)
    configureQaBuildType(project)
    configureGodToolsCustomUri()

    defaultConfig {
        applicationId = "org.keynote.godtools.android"
        versionName = "6.0.2-SNAPSHOT"
        versionCode = grgit.log(mapOf("includes" to listOf("HEAD"))).size + 4029265

        proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
        proguardFile("proguard-rules.pro")
        proguardFile("proguard-rules-crashlytics.pro")
        proguardFile("proguard-rules-eventbus.pro")
        proguardFile("proguard-rules-guava.pro")
        proguardFile("proguard-rules-okta-oidc.pro")
        proguardFile("proguard-searchview.pro")

        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "OKTA_CLIENT_ID", "\"0oa1ju0zx08vYGgbB0h8\"")
        buildConfigField("String", "OKTA_DISCOVERY_URI", "\"https://signon.okta.com\"")
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    createEventBusIndex("org.cru.godtools.AppEventBusIndex")
    kapt {
        javacOptions {
            option("-Adagger.fastInit=enabled")
        }
    }

    productFlavors {
        named("stage") {
            buildConfigField("String", "MOBILE_CONTENT_API", "\"$URI_MOBILE_CONTENT_API_STAGE\"")
        }
        named("production") {
            buildConfigField("String", "MOBILE_CONTENT_API", "\"$URI_MOBILE_CONTENT_API_PRODUCTION\"")
        }
    }

    signingConfigs {
        register("release") {
            storeFile = project.properties["androidKeystorePath"]?.let { rootProject.file(it) }
            storePassword = project.properties["androidKeystoreStorePassword"]?.toString()
            keyAlias = project.properties["androidKeystoreKeyAlias"]?.toString()
            keyPassword = project.properties["androidKeystoreKeyPassword"]?.toString()
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            isMinifyEnabled = false

            manifestPlaceholders += "webAuthenticationRedirectScheme" to "org.cru.godtools.debug.okta"
            buildConfigField("String", "OKTA_AUTH_SCHEME", "\"org.cru.godtools.debug.okta\"")
            resValue("string", "app_name_debug", "GodTools (Dev)")
        }
        named("qa") {
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"

            isMinifyEnabled = true

            manifestPlaceholders += "webAuthenticationRedirectScheme" to "org.cru.godtools.qa.okta"
            buildConfigField("String", "OKTA_AUTH_SCHEME", "\"org.cru.godtools.qa.okta\"")
            resValue("string", "app_name_debug", "GodTools (QA)")
        }
        release {
            isMinifyEnabled = true
            signingConfigs.getByName("release")
                .takeIf { it.storeFile?.exists() == true }
                ?.let { signingConfig = it }

            manifestPlaceholders += "webAuthenticationRedirectScheme" to "org.cru.godtools.okta"
            buildConfigField("String", "OKTA_AUTH_SCHEME", "\"org.cru.godtools.okta\"")
        }
    }
    bundle {
        language.enableSplit = false
    }
    dynamicFeatures += ":feature:bundledcontent"
}

onesky {
    sourceStringFiles = listOf(
        "strings.xml",
        "strings_about.xml",
        "strings_dashboard.xml",
        "strings_features.xml",
        "strings_profile.xml",
        "strings_tool_details.xml",
    )
}

dependencies {
    coreLibraryDesugaring(libs.android.desugaring)

    api(project(":library:api"))
    api(project(":library:db"))
    api(project(":library:download-manager"))
    implementation(project(":library:account"))
    implementation(project(":library:base"))
    implementation(project(":library:model"))
    implementation(project(":library:sync"))
    implementation(project(":library:user-data"))
    implementation(project(":ui:article-renderer"))
    implementation(project(":ui:base"))
    implementation(project(":ui:cyoa-renderer"))
    implementation(project(":ui:lesson-renderer"))
    implementation(project(":ui:shortcuts"))
    implementation(project(":ui:tract-renderer"))
    implementation(project(":ui:tutorial-renderer"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.work)

    implementation(libs.gtoSupport.androidx.compose)
    implementation(libs.gtoSupport.androidx.compose.material3)
    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.androidx.drawerlayout)
    implementation(libs.gtoSupport.androidx.fragment)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.androidx.recyclerview)
    implementation(libs.gtoSupport.androidx.viewpager2)
    implementation(libs.gtoSupport.androidx.work)
    implementation(libs.gtoSupport.appcompat)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.eventbus)
    implementation(libs.gtoSupport.firebase.crashlytics)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.lottie)
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.napier)
    implementation(libs.gtoSupport.picasso)
    implementation(libs.gtoSupport.util)

    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.inappmessaging)
    implementation(libs.firebase.perf)

    implementation(libs.play.core)
    implementation(libs.play.instantapps)

    api(libs.eventbus)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.coil.compose)
    implementation(libs.compose.reorderable)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.lottie)
    implementation(libs.lottie.compose)
    implementation(libs.materialColorUtilities)
    implementation(libs.materialComponents)
    implementation(libs.splitties.fragmentargs)
    implementation(libs.taptargetview)
    implementation(libs.weakdelegate)
    implementation(libs.youtubePlayer)

    debugImplementation(libs.facebook.flipper)
    debugImplementation(libs.facebook.flipper.plugins.leakcanary2)
    debugImplementation(libs.facebook.flipper.plugins.network)
    debugImplementation(libs.facebook.soloader)
    debugImplementation(libs.firebase.crashlytics.ndk)
    debugImplementation(libs.gtoSupport.facebook.flipper)
    debugImplementation(libs.gtoSupport.leakcanary)
    debugImplementation(libs.gtoSupport.okhttp3)
    debugImplementation(libs.leakcanary)

    kapt(libs.dagger.compiler)
    kapt(libs.eventbus.annotationProcessor)
    kapt(libs.hilt.compiler)

    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.gtoSupport.testing.dagger)
    testImplementation(libs.hilt.testing)

    kaptTest(libs.hilt.compiler)
}

// region Firebase App Distribution
if (project.hasProperty("firebaseAppDistributionBuild")) {
    firebaseAppDistribution {
        artifactPath =
            buildDir.resolve("outputs/apk_from_bundle/productionQa/app-production-qa-universal.apk").path
        releaseNotes = generateFirebaseAppDistributionReleaseNotes()
        serviceCredentialsFile = rootProject.file("firebase/firebase_api_key.json").path
        groups = "android-testers"
    }

    android.buildTypes.named("qa") {
        signingConfig = android.signingConfigs.create("firebaseAppDistribution") {
            storeFile = project.properties["firebaseAppDistributionKeystorePath"]?.let { rootProject.file(it) }
            storePassword = project.properties["firebaseAppDistributionKeystoreStorePassword"]?.toString()
            keyAlias = project.properties["firebaseAppDistributionKeystoreKeyAlias"]?.toString()
            keyPassword = project.properties["firebaseAppDistributionKeystoreKeyPassword"]?.toString()
        }
    }
}

fun generateFirebaseAppDistributionReleaseNotes(size: Int = 10) = buildString {
    append("Recent changes:\n\n")
    grgit.log(mapOf("maxCommits" to size)).forEach {
        append("* ").append(it.shortMessage).append('\n')
    }
}
// endregion Firebase App Distribution
