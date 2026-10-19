plugins {
    id("com.android.application")
}

val portalAssets = layout.buildDirectory.dir("generated/portalAssets")

android {
    namespace = "com.tc.vacuumstudy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tc.vacuumstudy"
        minSdk = 23
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file(rootProject.providers.gradleProperty("tcDebugKeystore").get())
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    androidResources {
        noCompress += "pdf"
    }

    sourceSets["main"].assets.srcDir(portalAssets)
}

// Use a fresh, project-local directory to keep OneDrive sync locks out of Gradle's temp workspace.
val externalBuildDirectory = System.getenv("TC_VACUUM_BUILD_DIR")
if (externalBuildDirectory.isNullOrBlank()) {
    layout.buildDirectory.set(layout.projectDirectory.dir("build-output"))
} else {
    layout.buildDirectory.set(file(externalBuildDirectory))
}

val preparePortalAssets by tasks.registering(Sync::class) {
    val portalRoot = System.getenv("TC_VACUUM_WEB_CONTENT_DIR")
        ?.takeIf { it.isNotBlank() }
        ?.let { file(it) }
        ?: rootProject.projectDir.parentFile.resolve("ChanKhongIOS/ChanKhong/WebContent")
    val htmlFile = portalRoot.resolve("index.html")
    val pdfFiles = listOf(
        "真空技術第0章至第4章--給學生(5).pdf" to "vacuum_lecture_00_04.pdf",
        "真空技術第5章至第6章--給學生(5).pdf" to "vacuum_lecture_05_06.pdf",
        "光學薄膜技術講義印製--新--給學生(4).pdf" to "optical_film_lecture.pdf",
    )
    val webAssets = listOf(
        "tc-vacuum-icon.png",
        "apple-touch-icon.png",
        "icon-192.png",
        "icon-512.png",
        "manifest.webmanifest",
        "sw.js",
    )

    from(htmlFile) {
        rename { "index.html" }
        filteringCharset = "UTF-8"
        filter { line ->
            var output = line.replace("target=\"_blank\"", "target=\"_self\"")
            pdfFiles.forEach { (sourceName, assetName) ->
                output = output.replace("href=\"./$sourceName\"", "href=\"./$assetName\"")
            }
            output
        }
    }
    pdfFiles.forEach { (sourceName, assetName) ->
        from(portalRoot.resolve(sourceName)) {
            rename { assetName }
        }
    }
    webAssets.forEach { assetName ->
        from(portalRoot.resolve(assetName))
    }
    into(portalAssets)
}

tasks.named("preBuild").configure {
    dependsOn(preparePortalAssets)
}
