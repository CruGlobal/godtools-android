plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
}

kotlin.jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.gradleDownloadTask)
    implementation(libs.json)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.kotlin.kover.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.ktlint.gradle)
}

ktlint {
    version.set(libs.versions.ktlint)

    filter {
        exclude { it.file in layout.buildDirectory.asFileTree }
    }
}
