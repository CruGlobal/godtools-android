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
    }
}

dependencies {
    compileOnly(gradleKotlinDsl())
    implementation(libs.android.gradlePlugin)
}
