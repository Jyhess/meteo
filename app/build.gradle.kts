import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// ---------------------------------------------------------------------------
// Helper: lit une propriété depuis local.properties (prioritaire) puis
// gradle.properties. Permet la surdéfinition locale sans modifier le dépôt.
// ---------------------------------------------------------------------------

fun loadPropertiesFile(file: File): Properties {
    val props = Properties()
    if (file.exists()) file.inputStream().use { props.load(it) }
    return props
}

fun resolveProperty(key: String, default: String = ""): String {
    val local  = loadPropertiesFile(rootProject.file("local.properties"))
    val gradle = loadPropertiesFile(rootProject.file("gradle.properties"))
    return local.getProperty(key) ?: gradle.getProperty(key) ?: default
}

/** Remplace la valeur d'une clé dans un fichier .properties sans altérer le reste. */
fun updatePropertyInFile(file: File, key: String, newValue: String) {
    val content = file.readText()
    val updated = if (content.contains(Regex("^$key=", RegexOption.MULTILINE))) {
        content.replace(Regex("^$key=.*", RegexOption.MULTILINE), "$key=$newValue")
    } else {
        content.trimEnd() + "\n$key=$newValue\n"
    }
    file.writeText(updated)
}

// ---------------------------------------------------------------------------
// Versions lues au moment de la configuration
// ---------------------------------------------------------------------------

val versionCodeProp = resolveProperty("VERSION_CODE", "1").toInt()
val versionNameProp = resolveProperty("VERSION_NAME", "1.0")

android {
    namespace = "com.meteo.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.meteo.app"
        minSdk = 26
        targetSdk = 34
        versionCode = versionCodeProp
        versionName = versionNameProp
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

// ---------------------------------------------------------------------------
// Version code commun (release / debug)
// ---------------------------------------------------------------------------

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Workaround for https://youtrack.jetbrains.com/issue/KT-83266
// Disable Compose mapping tasks that fail in AGP 9.0 + Kotlin 2.3.21
tasks.configureEach {
    if (name.contains("ComposeMapping", ignoreCase = true)) {
        enabled = false
    }
}

// ---------------------------------------------------------------------------
// Tâches de release/debug : incrémente le versionCode, build, copie l'APK.
// Le répertoire de destination est lu depuis local.properties (surdéfinition
// locale) ou depuis gradle.properties (valeur par défaut).
// Nom du fichier : app-<variant>-<VERSION_NAME>.apk
// ---------------------------------------------------------------------------

tasks.register("newRelease") {
    group = "meteo"
    description = "Incrémente VERSION_CODE, assemble le build release et copie l'APK."
    dependsOn("assembleRelease")
    doFirst {
        val propsFile = rootProject.file("gradle.properties")
        val currentCode = resolveProperty("VERSION_CODE", "1").toInt()
        val newCode = currentCode + 1
        updatePropertyInFile(propsFile, "VERSION_CODE", newCode.toString())
        println("VERSION_CODE : $currentCode → $newCode")
    }
    doLast {
        val newCode = resolveProperty("VERSION_CODE", "1").toInt()
        val versionName = resolveProperty("VERSION_NAME", "1.0")
        val outputDir   = resolveProperty("APK_OUTPUT_RELEASE", "app/build/outputs/apk/release/")
        val sourceApk   = project.layout.buildDirectory
            .file("outputs/apk/release/app-release.apk").get().asFile
        val destDir  = rootProject.file(outputDir)
        destDir.mkdirs()
        val destFile = File(destDir, "app-release-$versionName-$newCode.apk")
        sourceApk.copyTo(destFile, overwrite = true)
        println("APK copié → ${destFile.absolutePath}")
    }
}

tasks.register("newDebug") {
    group = "meteo"
    description = "Incrémente VERSION_CODE, assemble le build debug et copie l'APK."
    dependsOn("assembleDebug")
    doFirst {
        val propsFile = rootProject.file("gradle.properties")
        val currentCode = resolveProperty("VERSION_CODE", "1").toInt()
        val newCode = currentCode + 1
        updatePropertyInFile(propsFile, "VERSION_CODE", newCode.toString())
        println("VERSION_CODE : $currentCode → $newCode")
    }
    doLast {
        val newCode = resolveProperty("VERSION_CODE", "1").toInt()
        val versionName = resolveProperty("VERSION_NAME", "1.0")
        val outputDir   = resolveProperty("APK_OUTPUT_DEBUG", "app/build/outputs/apk/debug/")
        val sourceApk   = project.layout.buildDirectory
            .file("outputs/apk/debug/app-debug.apk").get().asFile
        val destDir  = rootProject.file(outputDir)
        destDir.mkdirs()
        val destFile = File(destDir, "app-debug-$versionName-$newCode.apk")
        sourceApk.copyTo(destFile, overwrite = true)
        println("APK copié → ${destFile.absolutePath}")
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)

    implementation(libs.play.services.location)
    implementation(libs.core.ktx)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.archunit)
}

// ArchitectureTest reads docs/architecture.puml and ARCHITECTURE_DIAGRAM: rerun the tests when either changes.
tasks.withType<Test>().configureEach {
    inputs.file(rootProject.file("docs/architecture.puml"))
    inputs.property("architectureDiagram", providers.environmentVariable("ARCHITECTURE_DIAGRAM").orElse(""))
}
