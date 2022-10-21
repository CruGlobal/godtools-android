import co.brainly.onesky.OneSkyPluginExtension
import co.brainly.onesky.task.DEPRECATE_STRINGS_FLAG
import co.brainly.onesky.task.DownloadTranslationsTask
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

        // handle legacy locales for Hebrew & Indonesian
        tasks.named("downloadTranslations", DownloadTranslationsTask::class.java) {
            doLast {
                val valuesHe = file(sourcePath).resolve("values-he")
                val valuesIw = file(sourcePath).resolve("values-iw")
                val valuesIn = file(sourcePath).resolve("values-in")
                val valuesId = file(sourcePath).resolve("values-id")

                sourceStringFiles.forEach { file ->
                    // copy Locale(he) to legacy Locale(iw)
                    valuesHe.resolve(file).takeIf { it.exists() }
                        ?.copyTo(valuesIw.resolve(file), overwrite = true)

                    // copy legacy Locale(in) to Locale(id)
                    // the onesky plugin internally changes Locale(id) to Locale(in), so we need to reverse this copy
                    valuesIn.resolve(file).takeIf { it.exists() }
                        ?.copyTo(valuesId.resolve(file), overwrite = true)
                }
            }
        }
    }
}
