// ============================================================================
//  Pocket House Remodeling  —  Project Settings
//  Talk to your real room. Watch it transform. Walk around the result.
// ============================================================================

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // SceneView (Compose-native 3D/AR) + Filament live on Maven Central.
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "PocketRemodel"
include(":app")
