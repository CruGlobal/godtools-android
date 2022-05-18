import com.android.build.api.dsl.AndroidSourceSet
import dagger.hilt.android.plugin.util.capitalize
import de.undercouch.gradle.tasks.download.Download
import org.cru.godtools.gradle.bundledcontent.DownloadApiResourcesTask
import org.cru.godtools.gradle.bundledcontent.ExtractAttachmentsTask
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

val BUNDLED_TOOLS = listOf("kgp", "fourlaws", "satisfied", "teachmetoshare")
val BUNDLED_ATTACHMENTS = listOf("attr-banner", "attr-banner-about")

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

    // configure tools json download tasks
    val intermediateToolsJson = intermediatesDir.resolve("tools.json")
    val downloadToolsJsonTask = tasks.create<Download>("download${name.capitalize()}BundledToolsJson") {
        mustRunAfter("clean")

        src("${mobileContentApi}resources?filter[system]=GodTools&include=attachments,latest-translations.language")
        dest(intermediateToolsJson)
        retries(2)
        tempAndMove(true)
    }
    val pruneToolsJsonTask = tasks.create<PruneJsonApiResponseTask>("prune${name.capitalize()}BundledToolsJson") {
        dependsOn(downloadToolsJsonTask)
        input.set(intermediateToolsJson)
        removeAllRelationshipsFor = listOf("language")
        removeAttributesFor["resource"] = listOf(
            // attributes
            "manifest", "onesky-project-id", "total-views",
            // relationships
            "system", "translations", "latest-drafts-translations", "pages", "custom-manifests", "tips"
        )
        removeAttributesFor["attachment"] = listOf("file", "is-zipped")
        removeAttributesFor["language"] = listOf("direction")
        output.set(assetGenDir.resolve("tools.json"))
    }
    assetGenTask.configure { dependsOn(pruneToolsJsonTask) }

    val attachmentsCopyTask = tasks.register<Copy>("copy${name.capitalize()}BundledAttachments") {
        into(assetGenDir.resolve("attachments/"))
    }
    assetGenTask.configure { dependsOn(attachmentsCopyTask) }
    BUNDLED_TOOLS.forEach { tool ->
        val variant = "${name.capitalize()}${tool.capitalize()}"

        // configure bundled attachments download tasks
        val extractAttachmentsTask = tasks.register<ExtractAttachmentsTask>("extract${variant}BundledAttachments") {
            toolsJson.set(pruneToolsJsonTask.output)
            this.tool = tool
            attachments = BUNDLED_ATTACHMENTS
            output.set(intermediatesDir.resolve("tool/$tool/attachments.json"))
        }

        val downloadAttachmentsTask = tasks.register<DownloadApiResourcesTask>("download${variant}BundledAttachments") {
            resources.set(extractAttachmentsTask.flatMap { it.output })
            api = mobileContentApi
            output.set(intermediatesDir.resolve("attachments/$tool/"))
        }

        attachmentsCopyTask.configure { from(downloadAttachmentsTask.flatMap { it.output }) }
    }
}

apply(from = "download_initial_content.gradle")
