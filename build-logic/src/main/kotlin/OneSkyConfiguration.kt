import co.brainly.onesky.OneSkyPluginExtension
import co.brainly.onesky.task.DEPRECATE_STRINGS_FLAG
import co.brainly.onesky.task.DownloadTranslationsTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra

private const val PROP_API_KEY = "ONESKY_API_KEY"
private const val PROP_API_SECRET = "ONESKY_API_SECRET"

private val ONESKY_LANGUAGE_CODE_OVERRIDES = mapOf(
    "bn-rIN" to "kha",
    "ku-rTR" to "ckb",
    "lv-rLV" to "lv",
    "nl-rCW" to "pap",
)

fun Project.onesky(configuration: OneSkyPluginExtension.() -> Unit) {
    extra.set(DEPRECATE_STRINGS_FLAG, true)
    apply(plugin = "co.brainly.onesky")

    configure<OneSkyPluginExtension> {
        apiKey = findProperty(PROP_API_KEY)?.toString().orEmpty()
        apiSecret = findProperty(PROP_API_SECRET)?.toString().orEmpty()
        projectId = 253275

        downloadLanguages = listOf(
            "am",
            "ar",
            "bn",
            "de",
            "es",
            "fr",
            "hi",
            "id",
            "ja",
            "ko",
            "lv",
            "pt",
            "ru",
            "sw",
            "ur",
            "vi",
            "zh-CN",
            "zh-TW",
        )

        configuration()

        // handle legacy locales for Hebrew & Indonesian
        tasks.named("downloadTranslations", DownloadTranslationsTask::class.java) {
            doLast {
                // HACK: handle manually overridden language codes.
                //       For certain language codes that OneSky doesn't support we hijack other languages and
                //       replace the language code. It appears that the plugin uses the OneSky project languages API
                //       which doesn't correctly return the updated codes. So we need to manually correct that here.
                ONESKY_LANGUAGE_CODE_OVERRIDES.forEach { (orig, hack) ->
                    val valuesOrig = file(sourcePath).resolve("values-$orig")
                    val valuesHack = file(sourcePath).resolve("values-$hack")

                    sourceStringFiles.forEach { file ->
                        // move Locale(orig) to Locale(hack)
                        valuesOrig.resolve(file).takeIf { it.exists() }?.let {
                            it.copyTo(valuesHack.resolve(file), overwrite = true)
                            it.delete()
                        }
                    }
                }

                // duplicate some legacy locales to both the old and new directories
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
