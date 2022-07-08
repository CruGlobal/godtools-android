pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://cruglobal.jfrog.io/artifactory/maven-mobile/")
            content {
                includeGroup("org.ccci.gto.android")
                includeGroup("org.ccci.gto.android.testing")
                includeGroup("org.cru.godtools.kotlin")
                includeGroup("org.cru.mobile.fork.antlr-kotlin")
            }
        }
        maven {
            url = uri("https://jitpack.io")
            content {
                includeGroupByRegex("com\\.github\\..*")
                excludeGroup("com.github.ajalt.colormath")
            }
        }
        google()
        mavenCentral()
        jcenter {
            content {
                includeModule("com.duolingo.open", "rtl-viewpager")
                includeModule("com.sergivonavi", "materialbanner")
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
include("library:user-data")

include("ui:article-aem-renderer")
include("ui:article-renderer")
include("ui:base")
include("ui:base-tool")
include("ui:cyoa-renderer")
include("ui:lesson-renderer")
include("ui:shortcuts")
include("ui:tips-renderer")
include("ui:tract-renderer")
include("ui:tutorial-renderer")

include("app")

include("feature:bundledcontent")

// automatically accept the scans.gradle.com TOS when running in GHA
if (System.getenv("GITHUB_ACTIONS")?.toBoolean() == true) {
    extensions.findByName("gradleEnterprise")?.withGroovyBuilder {
        getProperty("buildScan").withGroovyBuilder {
            setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
            setProperty("termsOfServiceAgree", "yes")
        }
    }
}
