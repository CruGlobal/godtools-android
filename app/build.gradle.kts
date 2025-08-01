import com.google.firebase.appdistribution.gradle.AppDistributionExtension

plugins {
    id("godtools.application-conventions")
    alias(libs.plugins.firebase.appdistribution)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.google.services)
    alias(libs.plugins.grgit)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.mockposable)
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "org.cru.godtools"

    configureCompose(project, enableCircuit = true)
    configureGodToolsCustomUri()

    defaultConfig {
        applicationId = "org.keynote.godtools.android"
        versionName = project.version.toString()
        versionCode = grgit.log(mapOf("includes" to listOf("HEAD"))).size + 4029265

        proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
        proguardFile("proguard-rules.pro")
        proguardFile("proguard-rules-crashlytics.pro")
        proguardFile("proguard-rules-eventbus.pro")
        proguardFile("proguard-rules-firebase-inappmessaging.pro")
        proguardFile("proguard-rules-guava.pro")
        proguardFile("proguard-rules-retrofit2.pro")
        proguardFile("proguard-searchview.pro")

        vectorDrawables.useSupportLibrary = true
    }
    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }

    productFlavors {
        named("stage") {
            versionNameSuffix = "-stage"
            applicationIdSuffix = ".stage"

            buildConfigField("String", "MOBILE_CONTENT_API", "\"$URI_MOBILE_CONTENT_API_STAGE\"")

            // Facebook
            resValue("string", "facebook_app_id", "448969905944197")
            resValue("string", "fb_login_protocol_scheme", "fb448969905944197")
            resValue("string", "facebook_client_token", "be1edf48d86ed54a24951ededa62eda2")

            // Google
            buildConfigField(
                "String",
                "GOOGLE_SERVER_CLIENT_ID",
                "\"71275134527-nvu2ehje1j6g459ofg5aldn1n21fadpg.apps.googleusercontent.com\""
            )
        }
        named("production") {
            buildConfigField("String", "MOBILE_CONTENT_API", "\"$URI_MOBILE_CONTENT_API_PRODUCTION\"")

            // Facebook
            resValue("string", "facebook_app_id", "2236701616451487")
            resValue("string", "fb_login_protocol_scheme", "fb2236701616451487")
            resValue("string", "facebook_client_token", "3b6bf5b7c128a970337c4fa1860ffa6e")

            // Google
            buildConfigField(
                "String",
                "GOOGLE_SERVER_CLIENT_ID",
                "\"71275134527-h5adpeeefcevhhhng1ggi5ngn6ko6d3k.apps.googleusercontent.com\""
            )
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

            resValue("string", "app_name_debug", "GodTools (Dev)")
        }
        named("qa") {
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"

            isMinifyEnabled = true
            proguardFile("proguard-rules-flipper.pro")

            resValue("string", "app_name_debug", "GodTools (QA)")
        }
        release {
            isMinifyEnabled = true
            signingConfigs.getByName("release")
                .takeIf { it.storeFile?.exists() == true }
                ?.let { signingConfig = it }
        }
    }
    bundle {
        density.enableSplit = false
        language.enableSplit = false
    }
    dynamicFeatures += ":feature:bundledcontent"
}

ksp {
    arg("dagger.fastInit", "enabled")
}

dependencies {
    api(project(":library:api"))
    api(project(":library:base"))
    api(project(":library:db"))
    api(project(":library:download-manager"))
    implementation(project(":library:account"))
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
    implementation(libs.androidx.compose.material3.windowsizeclass)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.work)

    implementation(libs.gtoSupport.androidx.compose)
    implementation(libs.gtoSupport.androidx.compose.material3)
    implementation(libs.gtoSupport.androidx.core)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.androidx.work)
    implementation(libs.gtoSupport.appcompat)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.eventbus)
    implementation(libs.gtoSupport.firebase.crashlytics)
    implementation(libs.gtoSupport.kermit)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.lottie)
    implementation(libs.gtoSupport.util)

    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.inappmessaging)
    implementation(libs.firebase.perf)

    implementation(libs.play.app.update.ktx)
    implementation(libs.play.feature.delivery)
    implementation(libs.play.instantapps)
    implementation(libs.play.review)

    implementation(libs.godtoolsShared.common)

    api(libs.eventbus)
    implementation(libs.circuitx.android)
    implementation(libs.circuitx.effects)
    implementation(libs.circuit.overlay)
    implementation(libs.coil.compose)
    implementation(libs.compose.reorderable)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.lottie)
    implementation(libs.lottie.compose)
    implementation(libs.youtubePlayer)

    debugImplementation(libs.androidx.compose.runtime.tracing)
    debugImplementation(libs.facebook.flipper)
    debugImplementation(libs.facebook.flipper.plugins.leakcanary2)
    debugImplementation(libs.facebook.flipper.plugins.network)
    debugImplementation(libs.facebook.soloader)
    debugImplementation(libs.firebase.crashlytics.ndk)
    debugImplementation(libs.gtoSupport.facebook.flipper)
    debugImplementation(libs.gtoSupport.leakcanary)
    debugImplementation(libs.gtoSupport.okhttp3)
    debugImplementation(libs.leakcanary)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testApi(testFixtures(libs.gtoSupport.androidx.compose))
    testImplementation(testFixtures(project(":library:model")))
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.androidx.test.espresso.core)
    testImplementation(libs.coil.test)
    testImplementation(libs.gtoSupport.testing.dagger)
    testImplementation(libs.hilt.testing)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.testparameterinjector)
    testImplementation(libs.turbine)
}

// region Firebase App Distribution
if (project.hasProperty("firebaseAppDistributionBuild")) {
    firebaseAppDistribution {
        releaseNotes = generateFirebaseAppDistributionReleaseNotes()
        serviceCredentialsFile = rootProject.file("firebase/firebase_api_key.json").path
        groups = "android-testers"
    }

    android {
        buildTypes.named("qa") {
            signingConfig = android.signingConfigs.create("firebaseAppDistribution") {
                storeFile = project.properties["firebaseAppDistributionKeystorePath"]?.let { rootProject.file(it) }
                storePassword = project.properties["firebaseAppDistributionKeystoreStorePassword"]?.toString()
                keyAlias = project.properties["firebaseAppDistributionKeystoreKeyAlias"]?.toString()
                keyPassword = project.properties["firebaseAppDistributionKeystoreKeyPassword"]?.toString()
            }
        }

        // we need to use the universal APK for Firebase App Distribution
        // TODO: hardcoding an apk path is very brittle and doesn't support buildTypes other than QA
        productFlavors {
            named("stage") {
                extensions.configure<AppDistributionExtension> {
                    artifactPath = layout.buildDirectory
                        .file("outputs/apk_from_bundle/stageQa/app-stage-qa-universal.apk")
                        .get().asFile.path
                }
            }
            named("production") {
                extensions.configure<AppDistributionExtension> {
                    artifactPath = layout.buildDirectory
                        .file("outputs/apk_from_bundle/productionQa/app-production-qa-universal.apk")
                        .get().asFile.path
                }
            }
        }
    }
}

// configure mockposable
mockposable {
    plugins = listOf("mockk")
}

fun generateFirebaseAppDistributionReleaseNotes(size: Int = 10) = buildString {
    append("Recent changes:\n\n")
    grgit.log(mapOf("maxCommits" to size)).forEach {
        append("* ").append(it.shortMessage).append('\n')
    }
}

afterEvaluate {
    android.applicationVariants.all { variant ->
        val packageUniversalApkTask = project.tasks.getByName("package${variant.name.capitalize()}UniversalApk")

        val appDistributionTask = project.tasks.getByName("appDistributionUpload${variant.name.capitalize()}")
        appDistributionTask.dependsOn.add(packageUniversalApkTask)
    }
}
// endregion Firebase App Distribution
