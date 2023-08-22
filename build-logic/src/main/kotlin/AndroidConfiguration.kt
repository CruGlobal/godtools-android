import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.TestedExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

internal const val BUILD_TYPE_DEBUG = "debug"
internal const val BUILD_TYPE_QA = "qa"
const val FLAVOR_DIMENSION_ENV = "env"
const val FLAVOR_ENV_STAGE = "stage"
internal const val FLAVOR_ENV_PRODUCTION = "production"

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
// context(Project)
internal fun TestedExtension.configureAndroidCommon(project: Project) {
    configureSdk()
    configureCompilerOptions(project)
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
    compileSdkVersion(34)

    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }
}

private fun BaseExtension.configureCompilerOptions(project: Project) {
    project.extensions.findByType<KotlinAndroidProjectExtension>()?.apply {
        jvmToolchain(17)
    }

    compileOptions {
        // HACK: workaround a kotlin.jvmToolchain bug
        //       see: https://issuetracker.google.com/issues/260059413
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    (this as ExtensionAware).extensions.findByType<KotlinJvmOptions>()?.apply {
        freeCompilerArgs += "-Xjvm-default=all"
    }
}

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
// context(Project)
fun BaseExtension.configureFlavorDimensions(project: Project) {
    flavorDimensions(FLAVOR_DIMENSION_ENV)
    productFlavors {
        register(FLAVOR_ENV_PRODUCTION) { dimension = FLAVOR_DIMENSION_ENV }
        register(FLAVOR_ENV_STAGE) {
            dimension = FLAVOR_DIMENSION_ENV

            // only enable this flavor for debug buildTypes
            project.extensions.configure<AndroidComponentsExtension<*, *, *>>("androidComponents") {
                beforeVariants(selector().withFlavor(FLAVOR_DIMENSION_ENV to FLAVOR_ENV_STAGE)) {
                    it.enable = it.buildType == "debug"
                }
            }
        }
    }
}

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
// context(Project)
fun CommonExtension<*, *, *, *, *>.configureCompose(project: Project) {
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion =
        project.libs.findVersion("androidx-compose-compiler").get().requiredVersion

    // add our base compose dependencies
    project.dependencies.apply {
        addProvider("implementation", project.libs.findBundle("androidx-compose").get())
        addProvider("debugImplementation", project.libs.findBundle("androidx-compose-debug").get())
        addProvider("testDebugImplementation", project.libs.findBundle("androidx-compose-testing").get())
    }
}

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
// context(Project)
fun CommonExtension<*, *, *, *, *>.configureQaBuildType(project: Project) {
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
