import com.android.build.api.dsl.AndroidSourceSet
import de.undercouch.gradle.tasks.download.Download
import org.cru.godtools.gradle.bundledcontent.PruneJsonApiResponseTask

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    baseConfiguration(project)
    configureFlavorDimensions()
}

dependencies {
    implementation(project(":library:base"))
    implementation(project(":library:db"))
    implementation(project(":library:download-manager"))
    implementation(project(":library:model"))

    implementation(libs.kotlin.coroutines)

    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.jsonapi)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.hilt)

    testImplementation(libs.kotlin.coroutines.test)

    kapt(libs.hilt.compiler)
}

apply(from = "download_initial_content.gradle")

android.libraryVariants.configureEach {
    val assetGenTask = tasks.named("generate${name.capitalize()}Assets")
    val mobileContentApi =
        if (flavorName.contains("stage")) URI_MOBILE_CONTENT_API_STAGE else URI_MOBILE_CONTENT_API_PRODUCTION
    val intermediatesDir = buildDir.resolve("intermediates/mobile_content_api/$dirName/")

    // create a generated assets directory for the initial content and add it to the most specific source set available.
    val assetGenDir = project.buildDir.resolve("generated/assets/initial-content/$dirName")
    sourceSets.filterIsInstance<AndroidSourceSet>().last().assets.srcDir(assetGenDir)

    // configure language json download tasks
    val intermediateLanguagesJson = intermediatesDir.resolve("languages.json")
    val downloadLanguagesJsonTask = tasks.create<Download>("download${name.capitalize()}LanguagesJson") {
        mustRunAfter("clean")

        src("${mobileContentApi}languages")
        dest(intermediateLanguagesJson)
        retries(2)
        quiet(false)
        tempAndMove(true)
    }
    val languagesJson = assetGenDir.resolve("languages.json")
    val pruneLanguagesJsonTask = tasks.create<PruneJsonApiResponseTask>("prune${name.capitalize()}LanguagesJson") {
        dependsOn(downloadLanguagesJsonTask)
        input.set(intermediateLanguagesJson)
        removeAllRelationshipsFor = listOf("language")
        removeAttributesFor["language"] = listOf("direction")
        output.set(languagesJson)
    }
    assetGenTask.configure { dependsOn(pruneLanguagesJsonTask) }
}
