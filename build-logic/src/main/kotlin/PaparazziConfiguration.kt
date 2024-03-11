import org.gradle.api.Project
import org.gradle.api.attributes.java.TargetJvmEnvironment

internal fun Project.fixGuavaDependencyForPaparazzi() {
    plugins.withId("app.cash.paparazzi") {
        // Defer until afterEvaluate so that testImplementation is created by Android plugin.
        afterEvaluate {
            dependencies.constraints {
                add("testImplementation", "com.google.guava:guava") {
                    attributes {
                        attribute(
                            TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
                            objects.named(TargetJvmEnvironment::class.java, TargetJvmEnvironment.STANDARD_JVM)
                        )
                    }
                    because(
                        "LayoutLib and sdk-common depend on Guava's -jre published variant. " +
                            "See https://github.com/cashapp/paparazzi/issues/906."
                    )
                }
            }
        }
    }
}
