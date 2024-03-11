import com.android.build.gradle.TestedExtension
import kotlinx.kover.gradle.plugin.dsl.KoverReportExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
// context(Project)
internal fun TestedExtension.configureTestOptions(project: Project) {
    testOptions.unitTests {
        isIncludeAndroidResources = true

        all {
            // default is 512MB, robolectric consumes a lot of memory
            // by loading an AOSP image for each version being tested
            it.maxHeapSize = "3500m"
        }
    }

    project.dependencies.addProvider("testImplementation", project.libs.findBundle("test-framework").get())

    project.configurations.configureEach {
        resolutionStrategy {
            dependencySubstitution {
                // use the new condensed version of hamcrest
                val hamcrest = project.libs.findLibrary("hamcrest").get().get().toString()
                substitute(module("org.hamcrest:hamcrest-core")).using(module(hamcrest))
                substitute(module("org.hamcrest:hamcrest-library")).using(module(hamcrest))
            }
        }
    }

    // only enable unit tests for debug builds targeting production
    project.androidComponents {
        beforeVariants { builder ->
            val env = builder.productFlavors.toMap()[FLAVOR_DIMENSION_ENV] ?: FLAVOR_ENV_PRODUCTION
            builder.enableUnitTest = builder.buildType == BUILD_TYPE_DEBUG && env == FLAVOR_ENV_PRODUCTION
        }
    }

    // Test Sharding
    val shard = project.findProperty("testShard")?.toString()?.toIntOrNull()
    val totalShards = project.findProperty("testTotalShards")?.toString()?.toIntOrNull()
    if (shard != null && totalShards != null) {
        if (Math.floorMod(project.path.hashCode(), totalShards) != Math.floorMod(shard, totalShards)) {
            project.androidComponents.beforeVariants { it.enableUnitTest = false }
        }
    }

    // Kotlin Kover
    project.plugins.apply("org.jetbrains.kotlinx.kover")
    project.extensions.configure<KoverReportExtension> {
        project.androidComponents.onVariants {
            androidReports(it.name) {
                xml {
                    // HACK: update the report file so that codecov picks it up automatically
                    setReportFile(project.layout.buildDirectory.file("reports/kover/${it.name}/report.xml"))
                }
            }
        }
    }

    // Paparazzi
    project.fixGuavaDependencyForPaparazzi()
}
