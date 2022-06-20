import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.core.InternalBaseVariant
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.android.build.gradle.internal.dsl.DynamicFeatureExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

fun Project.configureAndroidFeature() = extensions.configure<DynamicFeatureExtension> {
    configureAndroidCommon(project)
    configureFlavorDimensions()

    dependencies {
        add("implementation", project(":app"))
    }
}

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
//context(Project)
fun BaseAppModuleExtension.baseConfiguration(project: Project) {
    configureAndroidCommon(project)
    configureFlavorDimensions()
}

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
//context(Project)
fun LibraryExtension.baseConfiguration(project: Project) {
    configureAndroidCommon(project)
}

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
//context(Project)
private fun TestedExtension.configureAndroidCommon(project: Project) {
    configureSdk()
    configureCompilerOptions()
    configureTestOptions()

    lintOptions.lintConfig = project.rootProject.file("analysis/lint/lint.xml")

    filterStageVariants()
}

private fun BaseExtension.configureSdk() {
    compileSdkVersion(32)

    defaultConfig {
        minSdk = 21
        targetSdk = 32
    }
}

private fun BaseExtension.configureCompilerOptions() {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    (this as ExtensionAware).extensions.findByType<KotlinJvmOptions>()?.apply {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs += "-Xjvm-default=all"
    }
}

fun BaseExtension.configureFlavorDimensions() {
    flavorDimensions("env")
    productFlavors {
        create("stage").dimension = "env"
        create("production").dimension = "env"
    }
}

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
//context(Project)
fun CommonExtension<*, *, *, *>.configureCompose(project: Project, enableTesting: Boolean = false) {
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion =
        project.libs.findVersion("androidx-compose-compiler").get().requiredVersion

    // add our base compose dependencies
    project.dependencies.addProvider("implementation", project.libs.findBundle("androidx-compose").get())
    project.dependencies.addProvider("debugImplementation", project.libs.findBundle("androidx-compose-debug").get())

    if (enableTesting) {
        project.dependencies.addProvider(
            "testImplementation",
            project.libs.findBundle("androidx-compose-testing").get()
        )
        project.dependencies.addProvider(
            "debugImplementation",
            project.libs.findLibrary("androidx-compose-ui-test-manifest").get()
        )
    }
}

private fun TestedExtension.configureTestOptions() {
    testOptions.unitTests {
        isIncludeAndroidResources = true

        all {
            // default is 512MB, robolectric consumes a lot of memory
            // by loading an AOSP image for each version being tested
            it.maxHeapSize = "2g"
        }
    }

    testVariants.all { configureTestManifestPlaceholders() }
    unitTestVariants.all { configureTestManifestPlaceholders() }
}

private fun InternalBaseVariant.configureTestManifestPlaceholders() {
    mergedFlavor.manifestPlaceholders += "hostGodtoolsCustomUri" to "org.cru.godtools.test"
}

private fun BaseExtension.filterStageVariants() =
    variantFilter { if (flavors.any { it.name.contains("stage") } && buildType.name != "debug") ignore = true }
