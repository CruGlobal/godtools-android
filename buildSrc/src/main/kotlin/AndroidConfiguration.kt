import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.android.build.gradle.internal.dsl.DynamicFeatureExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

internal const val BUILD_TYPE_QA = "qa"
const val FLAVOR_DIMENSION_ENV = "env"
const val FLAVOR_ENV_STAGE = "stage"
private const val FLAVOR_ENV_PRODUCTION = "production"

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
//context(Project)
fun BaseAppModuleExtension.baseConfiguration(project: Project) {
    configureAndroidCommon(project)
    configureFlavorDimensions(project)
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
fun DynamicFeatureExtension.baseConfiguration(project: Project) {
    configureAndroidCommon(project)
    configureQaBuildType(project)
    configureFlavorDimensions(project)

    project.dependencies {
        add("implementation", project.project(":app"))
    }
}

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
//context(Project)
private fun TestedExtension.configureAndroidCommon(project: Project) {
    configureSdk()
    configureCompilerOptions(project)
    configureTestOptions(project)

    lintOptions.lintConfig = project.rootProject.file("analysis/lint/lint.xml")
}

private fun BaseExtension.configureSdk() {
    compileSdkVersion(33)

    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }
}

private fun BaseExtension.configureCompilerOptions(project: Project) {
    project.extensions.findByType<KotlinAndroidProjectExtension>()?.apply {
        jvmToolchain(11)
    }

    compileOptions {
        // HACK: workaround a kotlin.jvmToolchain bug
        //       see: https://issuetracker.google.com/issues/260059413
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    (this as ExtensionAware).extensions.findByType<KotlinJvmOptions>()?.apply {
        freeCompilerArgs += "-Xjvm-default=all"
    }
}

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
//context(Project)
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
//context(Project)
fun CommonExtension<*, *, *, *>.configureCompose(project: Project) {
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
//context(Project)
fun CommonExtension<*, *, *, *>.configureQaBuildType(project: Project) {
    buildTypes {
        register(BUILD_TYPE_QA) {
            initWith(getByName("debug"))
            matchingFallbacks += listOf("debug")
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
        named("${BUILD_TYPE_QA}Implementation") { extendsFrom(getByName("debugImplementation")) }
    }
}

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
//context(Project)
private fun TestedExtension.configureTestOptions(project: Project) {
    testOptions.unitTests {
        isIncludeAndroidResources = true

        all {
            // default is 512MB, robolectric consumes a lot of memory
            // by loading an AOSP image for each version being tested
            it.maxHeapSize = "2g"
        }
    }

    project.dependencies.apply {
        addProvider("testImplementation", project.libs.findBundle("test-framework").get())

        // HACK: Fix Manifest merge errors for any classpath that contains the Okta module
        addProvider("androidTestImplementation", testFixtures(project.libs.findLibrary("gtoSupport-okta").get()))
        addProvider("testImplementation", testFixtures(project.libs.findLibrary("gtoSupport-okta").get()))
    }

    project.configurations.configureEach {
        resolutionStrategy {
            dependencySubstitution {
                // use the new condensed version of hamcrest
                val hamcrest = project.libs.findLibrary("hamcrest").get().get().toString()
                substitute(module("org.hamcrest:hamcrest-core")).using(module(hamcrest))
                substitute(module("org.hamcrest:hamcrest-library")).using(module(hamcrest))
            }

            // HACK: work around a IllegalAccessException when using robolectric >= 4.6.1 + Espresso < 3.5.0
            // see: https://github.com/robolectric/robolectric/issues/6593
            // see: https://github.com/android/android-test/pull/1000
            val espressoCore = project.libs.findLibrary("androidx-test-espresso-core").get()
            force(espressoCore)
        }
    }
}
