import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.tasks.GenerateBuildConfig
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool

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
//       this is prototyped in 1.6.20 and will reach beta after Kotlin 2.0
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
//       this is prototyped in 1.6.20 and will reach beta after Kotlin 2.0
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

// TODO: Work around AGP generated BuildConfig not being exposed to KSP processors.
//       see: https://github.com/google/dagger/issues/4051
fun Project.exportAgpGeneratedSourcesToKsp() {
    androidComponents {
        onVariants { variant ->
            val kspTaskName = "ksp" + variant.name.capitalize() + "Kotlin"
            val buildConfigTaskName = "generate" + variant.name.capitalize() + "BuildConfig"

            afterEvaluate {
                tasks.named(kspTaskName) {
                    val buildConfigSource = tasks.named(buildConfigTaskName, GenerateBuildConfig::class.java)
                        .map { it.sourceOutputDir }

                    (this as AbstractKotlinCompileTool<*>).setSource(buildConfigSource)
                }
            }
        }
    }
}
