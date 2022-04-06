import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

fun Project.configureAndroidLibrary() {
    configureAndroidCommon()
}

fun Project.configureAndroidCommon() {
    extensions.configure<BaseExtension> {
        configureSdk()
        configureCompilerOptions()

        lintOptions.lintConfig = rootProject.file("analysis/lint/lint.xml")
        testOptions.unitTests.isIncludeAndroidResources = true
    }
}

private fun BaseExtension.configureSdk() {
    compileSdkVersion(32)

    defaultConfig {
        minSdk = 21
        targetSdk = 32
    }
}

private fun BaseExtension.configureCompilerOptions() {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    (this as ExtensionAware).extensions.findByType<KotlinJvmOptions>()?.apply {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs += "-Xjvm-default=all"
    }
}
