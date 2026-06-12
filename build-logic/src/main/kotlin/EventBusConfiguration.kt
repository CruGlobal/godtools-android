import org.gradle.api.Project

fun Project.createEventBusIndex(className: String) {
    plugins.apply("com.android.legacy-kapt")

    kapt {
        arguments {
            arg("eventBusIndex", className)
        }
    }

    dependencies.addProvider("kapt", libs.findLibrary("eventbus-annotationProcessor").get())
}
