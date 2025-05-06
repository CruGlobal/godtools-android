buildscript {
    dependencies {
        // HACK: load gradle-download-task before the kotlin plugin.
        //       This works around a classpath issue where the kotlin plugin includes an outdated version
        //       of public-suffix-list.txt
        //       see: https://github.com/michel-kraemer/gradle-download-task/issues/317#issuecomment-1635088067
        classpath(libs.gradleDownloadTask)

        classpath(libs.android.gradlePlugin)
        classpath(libs.kotlin.gradlePlugin)
    }
}

plugins {
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
