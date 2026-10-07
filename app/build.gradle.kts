import java.net.URL

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.aldiandrew.neobrutallauncher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aldiandrew.neobrutallauncher"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        getByName("debug")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

val neoBrutalFontBase =
    "https://raw.githubusercontent.com/ateliertriay/bricolage/84745e5b96261ae5f8c6c856e262fe78d1d6efdd/fonts/ttf/"

val neoBrutalFonts = mapOf(
    "bricolage_grotesque_regular.ttf" to "BricolageGrotesque-Regular.ttf",
    "bricolage_grotesque_bold.ttf" to "BricolageGrotesque-Bold.ttf",
    "bricolage_grotesque_extrabold.ttf" to "BricolageGrotesque-ExtraBold.ttf"
)

val downloadNeoBrutalFonts by tasks.registering {
    outputs.files(
        neoBrutalFonts.keys.map { fileName ->
            layout.projectDirectory.file("src/main/res/font/$fileName").asFile
        }
    )

    doLast {
        val destination = layout.projectDirectory.dir("src/main/res/font").asFile
        destination.mkdirs()

        neoBrutalFonts.forEach { (fileName, upstreamName) ->
            val target = destination.resolve(fileName)
            if (!target.exists() || target.length() == 0L) {
                URL(neoBrutalFontBase + upstreamName).openStream().use { input ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn(downloadNeoBrutalFonts)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.10.00")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
