plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
}

kotlin.jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.gradleDownloadTask)
    implementation(libs.json)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.kotlin.kover.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.ktlint.gradle)
    implementation(libs.onesky.gradlePlugin)
}

ktlint {
    version.set(libs.versions.ktlint)

    filter {
        exclude { it.file.path.startsWith("${buildDir.path}/") }
    }
}
