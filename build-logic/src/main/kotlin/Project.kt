import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

internal fun Project.androidComponents(configure: Action<AndroidComponentsExtension<*, *, *>>): Unit =
    extensions.configure("androidComponents", configure)
internal fun Project.kapt(configure: KaptExtension.() -> Unit) = extensions.configure(configure)

internal val Project.libs get() = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
internal val Project.androidComponents
    get() = project.extensions.getByName("androidComponents") as AndroidComponentsExtension<*, *, *>
