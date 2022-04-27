plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

configurations.configureEach {
    resolutionStrategy {
        // HACK: workaround a javapoet transitive dependency conflict between the android and hilt gradle plugins
        force(libs.javapoet)

        // HACK: workaround a transitive dependency version conflict in the kotlin gradle plugin
        force(libs.gradleDownloadTask)
    }
}

dependencies {
    compileOnly(gradleKotlinDsl())
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.json)
}
