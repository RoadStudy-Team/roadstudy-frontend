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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Pleos Connect SDK (Vehicle, NaviHelper, TextToSpeech). TextToSpeech 는 보안 모듈(security 그룹)도 받아야 한다
        maven {
            url = uri("https://nexus-playground.pleos.ai/repository/maven-releases/")
            content {
                includeGroup("ai.pleos.playground")
                includeGroup("ai.pleos.playground.security")
            }
        }
    }
}

rootProject.name = "roadstudy-frontend"
include(":mobile")
include(":shared")
include(":automotive")
 