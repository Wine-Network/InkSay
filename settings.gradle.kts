pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://api.xposed.info")
        maven("https://maven.aliyun.com/repository/public")
    }
}
rootProject.name = "LikSay"
include(":app")
include(":blockmiui")
include(":xtoast")