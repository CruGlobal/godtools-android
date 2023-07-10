import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs get() = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
internal val Project.androidComponents
    get() = project.extensions.getByName("androidComponents") as AndroidComponentsExtension<*, *, *>
