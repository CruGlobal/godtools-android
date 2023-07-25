import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.VariantDimension

private const val BUILD_CONFIG_FIELD_HOST = "HOST_GODTOOLS_CUSTOM_URI"
private const val PLACEHOLDER_HOST = "hostGodtoolsCustomUri"

fun CommonExtension<*, *, *, *, *>.configureGodToolsCustomUri() {
    buildFeatures.buildConfig = true
    buildTypes {
        named("debug") {
            setHost("org.cru.godtools.debug")
        }
        named(BUILD_TYPE_QA) {
            setHost("org.cru.godtools.qa")
        }
        named("release") {
            setHost("org.cru.godtools")
        }
    }
}

private fun VariantDimension.setHost(host: String) {
    manifestPlaceholders += PLACEHOLDER_HOST to host
    buildConfigField("String", BUILD_CONFIG_FIELD_HOST, "\"$host\"")
}
