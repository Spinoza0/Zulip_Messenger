pluginManagement {
    repositories {
        gradlePluginPortal()

        mavenCentral()
        google()
    }
}


dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        maven("https://jitpack.io")

        mavenCentral()
        google()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = ("homework")
include("app")
