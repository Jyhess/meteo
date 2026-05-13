import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
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

val versionCodeRelease = resolveProperty("VERSION_CODE_RELEASE", "1").toInt()
val versionCodeDebug   = resolveProperty("VERSION_CODE_DEBUG",   "1").toInt()
val versionNameProp    = resolveProperty("VERSION_NAME", "1.0")

android {
    namespace = "com.meteo.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.meteo.app"
        minSdk = 26
        targetSdk = 34
        versionCode = versionCodeRelease   // valeur par défaut (overridée par variant)
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
// Version code par variant (release / debug indépendants)
// ---------------------------------------------------------------------------

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            output.versionCode.set(
                when (variant.buildType) {
                    "release" -> versionCodeRelease
                    else      -> versionCodeDebug
                }
            )
        }
    }
}

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
    description = "Incrémente VERSION_CODE_RELEASE, assemble le build release et copie l'APK."
    dependsOn("assembleRelease")
    doFirst {
        val propsFile = rootProject.file("gradle.properties")
        val currentCode = resolveProperty("VERSION_CODE_RELEASE", "1").toInt()
        val newCode = currentCode + 1
        updatePropertyInFile(propsFile, "VERSION_CODE_RELEASE", newCode.toString())
        println("VERSION_CODE_RELEASE : $currentCode → $newCode")
    }
    doLast {
        val newCode = resolveProperty("VERSION_CODE_RELEASE", "1").toInt()
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
    description = "Incrémente VERSION_CODE_DEBUG, assemble le build debug et copie l'APK."
    dependsOn("assembleDebug")
    doFirst {
        val propsFile = rootProject.file("gradle.properties")
        val currentCode = resolveProperty("VERSION_CODE_DEBUG", "1").toInt()
        val newCode = currentCode + 1
        updatePropertyInFile(propsFile, "VERSION_CODE_DEBUG", newCode.toString())
        println("VERSION_CODE_DEBUG : $currentCode → $newCode")
    }
    doLast {
        val newCode = resolveProperty("VERSION_CODE_DEBUG", "1").toInt()
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
    val composeBom = platform("androidx.compose:compose-bom:2024.05.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.google.code.gson:gson:2.14.0")

    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.core:core-ktx:1.18.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
