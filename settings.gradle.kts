dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(
    ":app",
    ":base-android",
    ":base",
    ":navigation",
    ":appModules:factList"
)

rootProject.buildFileName = "build.gradle.kts"
