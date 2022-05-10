pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
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
