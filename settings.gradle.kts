pluginManagement {
    repositories {
        maven {
            setUrl("https://jitpack.io")
            content { includeGroupByRegex("com\\.github\\..*") }
        }
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            // HACK: use the master-SNAPSHOT version until 0.17.0 is released. This provides a better mechanism to
            //       modify excluded classes.
            //       see: https://github.com/vanniktech/gradle-android-junit-jacoco-plugin/pull/173
            if (requested.id.id == "com.vanniktech.android.junit.jacoco") {
                useModule("com.github.vanniktech:gradle-android-junit-jacoco-plugin:master-SNAPSHOT")
            }
        }
    }
}
rootProject.name = "godtools"

include("library:analytics")
include("library:api")
include("library:base")
include("library:db")
include("library:download-manager")
include("library:initial-content")
include("library:model")
include("library:sync")

include("ui:article-aem-renderer")
include("ui:article-renderer")
include("ui:base")
include("ui:base-tool")
include("ui:lesson-renderer")
include("ui:shortcuts")
include("ui:tract-renderer")
include("ui:tutorial-renderer")

include("app")

include("feature:bundledcontent")
