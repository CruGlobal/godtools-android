import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

fun Project.createEventBusIndex(className: String) {
    (this as ExtensionAware).extensions.configure<KaptExtension> {
        arguments {
            arg("eventBusIndex", className)
        }
    }
}
