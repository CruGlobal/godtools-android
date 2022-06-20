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
    baseConfiguration(project)
    configureCompose(project, enableTesting = true)

    defaultConfig {
        applicationId = "org.keynote.godtools.android"
        versionName = "6.0.0-SNAPSHOT"
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

    packagingOptions {
        // XXX: Exclude Kotlin metadata to reduce the size of the APK.
        //      If we ever start utilizing kotlin reflection this will need to be removed.
        resources.excludes += "**/*.kotlin_*"
    }

    createEventBusIndex("org.cru.godtools.AppEventBusIndex")
    kapt {
        javacOptions {
            option("-Adagger.fastInit=enabled")
        }
    }

    productFlavors {
        val stage by existing {
            buildConfigField("String", "MOBILE_CONTENT_API", "\"$URI_MOBILE_CONTENT_API_STAGE\"")
        }
        val production by existing {
            buildConfigField("String", "MOBILE_CONTENT_API", "\"$URI_MOBILE_CONTENT_API_PRODUCTION\"")
        }
    }

    signingConfigs {
        val firebaseAppDistribution by creating {
            storeFile = project.properties["firebaseAppDistributionKeystorePath"]?.let { rootProject.file(it) }
            storePassword = project.properties["firebaseAppDistributionKeystoreStorePassword"]?.toString()
            keyAlias = project.properties["firebaseAppDistributionKeystoreKeyAlias"]?.toString()
            keyPassword = project.properties["firebaseAppDistributionKeystoreKeyPassword"]?.toString()
        }
        val release by creating {
            storeFile = project.properties["androidKeystorePath"]?.let { rootProject.file(it) }
            storePassword = project.properties["androidKeystoreStorePassword"]?.toString()
            keyAlias = project.properties["androidKeystoreKeyAlias"]?.toString()
            keyPassword = project.properties["androidKeystoreKeyPassword"]?.toString()
        }
    }

    buildTypes {
        val debug by getting {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            isMinifyEnabled = false
            isShrinkResources = false

            manifestPlaceholders += mapOf(
                "appAuthRedirectScheme" to "org.cru.godtools.debug.okta",
                "hostGodtoolsCustomUri" to "org.cru.godtools.debug"
            )

            buildConfigField("String", "HOST_GODTOOLS_CUSTOM_URI", "\"org.cru.godtools.debug\"")
            buildConfigField("String", "OKTA_AUTH_SCHEME", "\"org.cru.godtools.debug.okta\"")
            resValue("string", "app_name_debug", "GodTools (Dev)")
        }
        val qa by creating {
            initWith(debug)
            matchingFallbacks += listOf("debug")

            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"

            manifestPlaceholders += mapOf(
                "appAuthRedirectScheme" to "org.cru.godtools.qa.okta",
                "hostGodtoolsCustomUri" to "org.cru.godtools.qa"
            )

            buildConfigField("String", "HOST_GODTOOLS_CUSTOM_URI", "\"org.cru.godtools.qa\"")
            buildConfigField("String", "OKTA_AUTH_SCHEME", "\"org.cru.godtools.qa.okta\"")
            resValue("string", "app_name_debug", "GodTools (QA)")

            // Firebase App Distribution build
            if (project.hasProperty("firebaseAppDistributionBuild")) {
                signingConfig = signingConfigs.getByName("firebaseAppDistribution")

                firebaseAppDistribution {
                    artifactPath =
                        buildDir.resolve("outputs/universal_apk/productionQa/app-production-qa-universal.apk").path
                    releaseNotes = generateFirebaseAppDistributionReleaseNotes()
                    serviceCredentialsFile = rootProject.file("firebase/firebase_api_key.json").path
                    groups = "android-testers"
                }
            }
        }
        val release by existing {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfigs.getByName("release").takeIf { it.storeFile?.exists() == true }
                ?.let { signingConfig = it }

            manifestPlaceholders += mapOf(
                "appAuthRedirectScheme" to "org.cru.godtools.okta",
                "hostGodtoolsCustomUri" to "org.cru.godtools"
            )

            buildConfigField("String", "HOST_GODTOOLS_CUSTOM_URI", "\"org.cru.godtools\"")
            buildConfigField("String", "OKTA_AUTH_SCHEME", "\"org.cru.godtools.okta\"")
        }
    }
    bundle {
        language.enableSplit = false
    }
    dynamicFeatures += ":feature:bundledcontent"

    sourceSets {
        getByName("qa") {
            kotlin.srcDir("src/debug/kotlin")
            res.srcDir("src/debug/res/values")
            manifest.srcFile("src/debug/AndroidManifest.xml")
        }
    }
}

configurations {
    named("qaImplementation") { extendsFrom(getByName("debugImplementation")) }
}

dependencies {
    api(project(":library:api"))
    api(project(":library:db"))
    api(project(":library:download-manager"))
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

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.work)

    implementation(libs.gtoSupport.androidx.compose)
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
    implementation(libs.gtoSupport.lottie)
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.napier)
    implementation(libs.gtoSupport.okta)
    implementation(libs.gtoSupport.picasso)
    implementation(libs.gtoSupport.recyclerview.advrecyclerview)
    implementation(libs.gtoSupport.util)

    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.inappmessaging)
    implementation(libs.firebase.perf)

    implementation(libs.play.core)
    implementation(libs.play.instantapps)

    api(libs.eventbus)
    implementation(libs.advrecyclerview)
    implementation(libs.coil.compose)
    implementation(libs.godtoolsMpp.parser)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.lottie)
    implementation(libs.materialBanner)
    implementation(libs.materialComponents)
    implementation(libs.okta)
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

fun generateFirebaseAppDistributionReleaseNotes(size: Int = 10) = buildString {
    append("Recent changes:\n\n")
    grgit.log(mapOf("maxCommits" to size)).forEach {
        append("* ").append(it.shortMessage).append('\n')
    }
}
