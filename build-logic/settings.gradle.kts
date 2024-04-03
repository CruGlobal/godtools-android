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

// TODO: temporarily import our forked version of the onesky-gradle-plugin until
//       https://github.com/brainly/onesky-gradle-plugin/pull/12 is merged
//       see: https://jira.cru.org/browse/GT-2344
includeBuild("../imported/onesky-gradle-plugin")
