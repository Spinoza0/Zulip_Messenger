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

rootProject.name = ("homework")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "app"
)

