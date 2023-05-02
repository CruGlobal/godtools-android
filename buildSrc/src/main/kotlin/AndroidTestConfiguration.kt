import com.android.build.gradle.TestedExtension
import org.gradle.api.Project

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
//context(Project)
internal fun TestedExtension.configureTestOptions(project: Project) {
    testOptions.unitTests {
        isIncludeAndroidResources = true

        all {
            // default is 512MB, robolectric consumes a lot of memory
            // by loading an AOSP image for each version being tested
            it.maxHeapSize = "3g"
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

            // HACK: work around a IllegalAccessException when using robolectric >= 4.6.1 + Espresso < 3.5.0
            // see: https://github.com/robolectric/robolectric/issues/6593
            // see: https://github.com/android/android-test/pull/1000
            val espressoCore = project.libs.findLibrary("androidx-test-espresso-core").get()
            force(espressoCore)
        }
    }
}
