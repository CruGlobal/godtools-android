plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
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

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
