pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        // 作为 Xposed 模块使用务必添加，其它情况可选
        maven("https://api.xposed.info/" )
        // MavenCentral 有 2 小时缓存，若无法集成最新版本请添加此地址
        maven( "https://s01.oss.sonatype.org/content/repositories/releases/" )
    }
}

rootProject.name = "AnyDebugV2"
include(":app")
