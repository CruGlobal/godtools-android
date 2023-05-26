plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

kotlin.jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
}

configurations.configureEach {
    resolutionStrategy {
        // HACK: workaround a javapoet transitive dependency conflict between the android and hilt gradle plugins
        force(libs.javapoet)
    }
}

dependencies {
    compileOnly(gradleKotlinDsl())
    implementation(libs.android.gradlePlugin)
    implementation(libs.gradleDownloadTask)
    implementation(libs.json)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.kotlin.kover.gradlePlugin)
    implementation(libs.onesky.gradlePlugin)
}
