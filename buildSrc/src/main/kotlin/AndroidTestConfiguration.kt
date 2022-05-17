import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.core.InternalBaseVariant
import org.gradle.api.Project

private const val CATEGORY_ROBOLECTRIC = "androidx.test.ext.junit.runners.AndroidJUnit4"

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
//context(Project)
internal fun TestedExtension.configureTestOptions(project: Project) {
    testOptions.unitTests {
        isIncludeAndroidResources = true

        all { test ->
            test.useJUnit {
                // configure Test Sharding
                // 1: Robolectric
                // 2: Other Tests
                // null: Default Behavior, all tests run
                when (project.findProperty("testShard")?.toString()?.toIntOrNull()) {
                    1 -> includeCategories = setOf(CATEGORY_ROBOLECTRIC)
                    2 -> excludeCategories = setOf(CATEGORY_ROBOLECTRIC)
                    null -> Unit
                    else -> throw IllegalArgumentException("Invalid testShard: ${project.findProperty("testShard")}")
                }

                if (
                    (includeCategories.isEmpty() || CATEGORY_ROBOLECTRIC in includeCategories) &&
                    CATEGORY_ROBOLECTRIC !in excludeCategories
                ) {
                    // Robolectric consumes a lot of memory by loading an AOSP
                    // image for each version being tested, default maxHeapSize is 512MB
                    test.maxHeapSize = "2g"
                }
            }
        }
    }

    testVariants.all { configureTestManifestPlaceholders() }
    unitTestVariants.all { configureTestManifestPlaceholders() }

    project.dependencies.addProvider("testImplementation", project.libs.findBundle("test-framework").get())

    project.configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            // use the new condensed version of hamcrest
            val hamcrest = project.libs.findLibrary("hamcrest").get().get().toString()
            substitute(module("org.hamcrest:hamcrest-core")).using(module(hamcrest))
            substitute(module("org.hamcrest:hamcrest-library")).using(module(hamcrest))
        }
    }
}

private fun InternalBaseVariant.configureTestManifestPlaceholders() {
    mergedFlavor.manifestPlaceholders += "hostGodtoolsCustomUri" to "org.cru.godtools.test"
}
