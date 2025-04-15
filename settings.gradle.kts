pluginManagement {
    repositories {
        maven {
            url = uri("https://cruglobal.jfrog.io/artifactory/maven-mobile/")
            content {
                includeGroup("org.cru.mobile.fork.co.brainly")
            }
        }
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://cruglobal.jfrog.io/artifactory/maven-mobile/")
            content {
                includeGroup("org.ccci.gto.android")
                includeGroup("org.ccci.gto.android.testing")
                includeGroup("org.cru.godtools.kotlin")
                includeGroup("org.cru.mobile.fork.material-color-utilities")

                // Included groups to support renovate updates
                includeGroup("org.cru.mobile.fork.co.brainly")
            }
        }
        maven {
            url = uri("https://jitpack.io")
            content {
                includeGroupByRegex("com\\.github\\..*")
                excludeGroup("com.github.ajalt.colormath")
            }
        }
        maven {
            // This repository contains pre-release versions of the Compose Compiler
            url = uri("https://androidx.dev/storage/compose-compiler/repository/")
            content {
                includeGroup("androidx.compose.compiler")
            }
        }
        maven {
            // This repo is for resolution of the transitive kustomexport annotation dependency used in godtools-shared
            url = uri("https://raw.githubusercontent.com/Deezer/KustomExport/mvn-repo")
            content {
                includeGroup("deezer.kustomexport")
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "godtools"

includeBuild("build-logic")

include("library:account")
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
include("ui:opt-in-notification-renderer")
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
