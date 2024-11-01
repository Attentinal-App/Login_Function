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
        maven (url = "https://devrepo.kakao.com/nexus/content/groups/public/")
        maven (url = "https://naver.github.io/naver-login-sdk")
    }
}

rootProject.name = "CapstoneLogin"
include(":app")