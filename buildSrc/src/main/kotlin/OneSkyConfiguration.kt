import co.brainly.onesky.OneSkyPluginExtension
import co.brainly.onesky.task.DEPRECATE_STRINGS_FLAG
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra

private const val PROP_API_KEY = "ONESKY_API_KEY"
private const val PROP_API_SECRET = "ONESKY_API_SECRET"

fun Project.onesky(configuration: OneSkyPluginExtension.() -> Unit) {
    extra.set(DEPRECATE_STRINGS_FLAG, true)
    apply(plugin = "co.brainly.onesky")

    configure<OneSkyPluginExtension> {
        apiKey = findProperty(PROP_API_KEY)?.toString().orEmpty()
        apiSecret = findProperty(PROP_API_SECRET)?.toString().orEmpty()
        projectId = 253275

        // TODO: enable this after https://github.com/brainly/onesky-gradle-plugin/pull/6 is merged & released
        // downloadBaseLanguage = true

        configuration()
    }
}
