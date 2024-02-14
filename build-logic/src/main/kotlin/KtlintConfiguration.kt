import org.gradle.api.Project

fun Project.configureKtlint() {
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")

    ktlint {
        version.set(libs.findVersion("ktlint").get().requiredVersion)
    }
}
