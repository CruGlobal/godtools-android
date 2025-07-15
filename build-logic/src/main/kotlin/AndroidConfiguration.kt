import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.TestedExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal const val BUILD_TYPE_DEBUG = "debug"
internal const val BUILD_TYPE_QA = "qa"
const val FLAVOR_DIMENSION_ENV = "env"
const val FLAVOR_ENV_STAGE = "stage"
internal const val FLAVOR_ENV_PRODUCTION = "production"

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will reach beta after Kotlin 2.0
// context(Project)
internal fun TestedExtension.configureAndroidCommon(project: Project) {
    configureSdk()
    configureCompilerOptions(project)
    enableCoreLibraryDesugaring(project)
    project.configureCommonDependencies()
    configureTestOptions(project)

    lintOptions.lintConfig = project.rootProject.file("analysis/lint/lint.xml")
}

private fun Project.configureCommonDependencies() {
    dependencies.addProvider("implementation", libs.findBundle("common").get())

    // HACK: Google Guava compatibility
    configurations.configureEach {
        resolutionStrategy.capabilitiesResolution.withCapability("com.google.guava:listenablefuture") {
            candidates.firstOrNull { it.id.let { it is ModuleComponentIdentifier && it.module == "guava" } }
                ?.let { select(it) }

            because("Google Guava provides listenablefuture, so we should prefer that over the standalone artifact")
        }

        // exclude guava transitive compileOnly dependencies
        exclude(group = "com.google.j2objc", module = "j2objc-annotations")
    }
}

internal fun Project.excludeAndroidSdkDependencies() {
    configurations.configureEach {
        exclude(group = "org.json", module = "json")
    }
}

private fun BaseExtension.configureSdk() {
    compileSdkVersion(35)

    defaultConfig {
        minSdk = 21
        targetSdk = 35
    }
}

private fun BaseExtension.configureCompilerOptions(project: Project) {
    project.extensions.findByType<KotlinAndroidProjectExtension>()?.apply {
        jvmToolchain(21)
    }

    compileOptions {
        // HACK: workaround a kotlin.jvmToolchain bug
        //       see: https://issuetracker.google.com/issues/260059413
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

private fun BaseExtension.enableCoreLibraryDesugaring(project: Project) {
    compileOptions.isCoreLibraryDesugaringEnabled = true
    project.dependencies.addProvider("coreLibraryDesugaring", project.libs.findLibrary("android-desugaring").get())
}

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will reach beta after Kotlin 2.0
// context(Project)
fun BaseExtension.configureFlavorDimensions(project: Project) {
    flavorDimensions(FLAVOR_DIMENSION_ENV)
    productFlavors {
        register(FLAVOR_ENV_PRODUCTION) { dimension = FLAVOR_DIMENSION_ENV }
        register(FLAVOR_ENV_STAGE) {
            dimension = FLAVOR_DIMENSION_ENV

            // only enable this flavor for debug & QA buildTypes
            project.extensions.configure<AndroidComponentsExtension<*, *, *>>("androidComponents") {
                beforeVariants(selector().withFlavor(FLAVOR_DIMENSION_ENV to FLAVOR_ENV_STAGE)) {
                    it.enable = it.buildType == BUILD_TYPE_DEBUG || it.buildType == BUILD_TYPE_QA
                }
            }
        }
    }
}

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will reach beta after Kotlin 2.0
// context(Project)
fun CommonExtension<*, *, *, *, *, *>.configureCompose(project: Project, enableCircuit: Boolean = false) {
    buildFeatures.compose = true
    project.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

    // add our base compose dependencies
    project.dependencies.apply {
        addProvider("implementation", project.libs.findBundle("androidx-compose").get())
        addProvider("debugImplementation", project.libs.findBundle("androidx-compose-debug").get())
        addProvider("testDebugImplementation", project.libs.findBundle("androidx-compose-testing").get())
    }

    // configure circuit
    if (enableCircuit) {
        project.pluginManager.apply("com.google.devtools.ksp")
        project.pluginManager.apply("kotlin-parcelize")

        project.dependencies.addProvider("implementation", project.libs.findBundle("circuit").get())
        project.dependencies.addProvider("ksp", project.libs.findLibrary("circuit-codegen").get())

        project.ksp.arg("circuit.codegen.mode", "hilt")
    }
}

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will reach beta after Kotlin 2.0
// context(Project)
fun CommonExtension<*, *, *, *, *, *>.configureQaBuildType(project: Project) {
    buildTypes {
        register(BUILD_TYPE_QA) {
            initWith(getByName(BUILD_TYPE_DEBUG))
            matchingFallbacks += listOf(BUILD_TYPE_DEBUG)
        }
    }

    sourceSets {
        named(BUILD_TYPE_QA) {
            kotlin.srcDir("src/debug/kotlin")
            res.srcDir("src/debug/res/values")
            manifest.srcFile("src/debug/AndroidManifest.xml")
        }
    }

    project.configurations {
        named("${BUILD_TYPE_QA}Api") { extendsFrom(getByName("${BUILD_TYPE_DEBUG}Api")) }
        named("${BUILD_TYPE_QA}Implementation") { extendsFrom(getByName("${BUILD_TYPE_DEBUG}Implementation")) }
    }
}
