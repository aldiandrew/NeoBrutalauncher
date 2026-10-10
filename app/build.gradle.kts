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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

val neoBrutalFontSources = mapOf(
    "anton_regular.ttf" to "https://raw.githubusercontent.com/googlefonts/AntonFont/beb92fcad87808357123bb66881b4032dc96efe7/fonts/Anton-Regular.ttf",
    "space_grotesk_regular.ttf" to "https://raw.githubusercontent.com/floriankarsten/space-grotesk/03507d024a01282884232081fc6011c09ff4e849/fonts/ttf/static/SpaceGrotesk-Regular.ttf",
    "space_grotesk_bold.ttf" to "https://raw.githubusercontent.com/floriankarsten/space-grotesk/03507d024a01282884232081fc6011c09ff4e849/fonts/ttf/static/SpaceGrotesk-Bold.ttf"
)

val downloadNeoBrutalFonts by tasks.registering {
    outputs.files(
        neoBrutalFontSources.keys.map { fileName ->
            layout.projectDirectory.file("src/main/res/font/$fileName").asFile
        }
    )

    doLast {
        val destination = layout.projectDirectory.dir("src/main/res/font").asFile
        destination.mkdirs()

        neoBrutalFontSources.forEach { (fileName, url) ->
            val target = destination.resolve(fileName)
            if (!target.exists() || target.length() == 0L) {
                URL(url).openStream().use { input ->
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
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    testImplementation("junit:junit:4.13.2")
}
