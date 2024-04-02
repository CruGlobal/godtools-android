dependencyResolutionManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

// HACK: temporarily import our forked version of the onesky-gradle-plugin until
//       https://github.com/brainly/onesky-gradle-plugin/pull/12 is merged
includeBuild("../imported/onesky-gradle-plugin")
